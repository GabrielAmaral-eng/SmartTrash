create extension if not exists pgcrypto with schema extensions;

create schema if not exists private;

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  email text not null,
  full_name text,
  role text not null default 'OPERATOR' check (role in ('ADMIN', 'OPERATOR', 'VIEWER')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.smart_bins (
  id text primary key,
  name text not null,
  region text not null,
  bin_height_cm numeric(8, 2) not null check (bin_height_cm > 0),
  latitude numeric(10, 7) not null,
  longitude numeric(10, 7) not null,
  status text not null check (status in ('EMPTY', 'ATTENTION', 'FULL')),
  current_distance_cm numeric(8, 2) not null check (current_distance_cm >= 0),
  current_fill_level_percent numeric(5, 2) not null check (current_fill_level_percent between 0 and 100),
  last_update timestamptz not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.sensor_readings (
  id uuid primary key default gen_random_uuid(),
  sensor_id text not null references public.smart_bins(id) on delete cascade,
  recorded_at timestamptz not null,
  distance_cm numeric(8, 2) not null check (distance_cm >= 0),
  fill_level_percent numeric(5, 2) not null check (fill_level_percent between 0 and 100),
  created_at timestamptz not null default now(),
  unique (sensor_id, recorded_at)
);

create table if not exists public.collection_assignments (
  id text primary key default gen_random_uuid()::text,
  sensor_id text not null references public.smart_bins(id) on delete cascade,
  status text not null default 'SCHEDULED' check (status in ('SCHEDULED', 'IN_PROGRESS', 'COLLECTED')),
  departure_time timestamptz not null,
  estimated_collection_time timestamptz not null,
  responsible_team text not null,
  progress_percent integer not null default 0 check (progress_percent between 0 and 100),
  requested_by uuid references auth.users(id) on delete set null default auth.uid(),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists sensor_readings_sensor_recorded_at_idx
  on public.sensor_readings (sensor_id, recorded_at desc);

create index if not exists smart_bins_status_region_idx
  on public.smart_bins (status, region);

create index if not exists collection_assignments_sensor_status_idx
  on public.collection_assignments (sensor_id, status);

create or replace function private.set_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create or replace function private.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  insert into public.profiles (id, email, full_name)
  values (
    new.id,
    new.email,
    coalesce(new.raw_user_meta_data ->> 'full_name', split_part(new.email, '@', 1))
  )
  on conflict (id) do update
    set email = excluded.email,
        full_name = coalesce(public.profiles.full_name, excluded.full_name),
        updated_at = now();
  return new;
end;
$$;

drop trigger if exists set_profiles_updated_at on public.profiles;
create trigger set_profiles_updated_at
  before update on public.profiles
  for each row execute function private.set_updated_at();

drop trigger if exists set_smart_bins_updated_at on public.smart_bins;
create trigger set_smart_bins_updated_at
  before update on public.smart_bins
  for each row execute function private.set_updated_at();

drop trigger if exists set_collection_assignments_updated_at on public.collection_assignments;
create trigger set_collection_assignments_updated_at
  before update on public.collection_assignments
  for each row execute function private.set_updated_at();

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function private.handle_new_user();

alter table public.profiles enable row level security;
alter table public.smart_bins enable row level security;
alter table public.sensor_readings enable row level security;
alter table public.collection_assignments enable row level security;

drop policy if exists profiles_select_own on public.profiles;
create policy profiles_select_own
  on public.profiles for select
  to authenticated
  using ((select auth.uid()) = id);

drop policy if exists smart_bins_select_authenticated on public.smart_bins;
create policy smart_bins_select_authenticated
  on public.smart_bins for select
  to authenticated
  using (true);

drop policy if exists sensor_readings_select_authenticated on public.sensor_readings;
create policy sensor_readings_select_authenticated
  on public.sensor_readings for select
  to authenticated
  using (true);

drop policy if exists collection_assignments_select_authenticated on public.collection_assignments;
create policy collection_assignments_select_authenticated
  on public.collection_assignments for select
  to authenticated
  using (true);

drop policy if exists collection_assignments_insert_own on public.collection_assignments;
create policy collection_assignments_insert_own
  on public.collection_assignments for insert
  to authenticated
  with check ((select auth.uid()) is not null and requested_by = (select auth.uid()));

drop policy if exists collection_assignments_update_own on public.collection_assignments;
create policy collection_assignments_update_own
  on public.collection_assignments for update
  to authenticated
  using ((select auth.uid()) is not null and requested_by = (select auth.uid()))
  with check ((select auth.uid()) is not null and requested_by = (select auth.uid()));

grant usage on schema public to anon, authenticated;
grant select on public.profiles to authenticated;
grant select on public.smart_bins to authenticated;
grant select on public.sensor_readings to authenticated;
grant select, insert, update on public.collection_assignments to authenticated;

insert into public.smart_bins (id, name, region, latitude, longitude, bin_height_cm, status, current_distance_cm, current_fill_level_percent, last_update)
values
  ('bin-001', 'Lixeira Av. Paulista', 'Centro', -23.5614, -46.6559, 120, 'EMPTY', 84, 30, '2026-04-20T12:00:00Z'),
  ('bin-002', 'Lixeira Rua Augusta', 'Centro', -23.5558, -46.6581, 120, 'ATTENTION', 54, 55, '2026-04-20T12:00:00Z'),
  ('bin-003', 'Lixeira Parque Ibirapuera', 'Zona Sul', -23.5874, -46.6576, 120, 'FULL', 18, 85, '2026-04-20T12:00:00Z'),
  ('bin-004', 'Lixeira Vila Madalena', 'Zona Oeste', -23.5503, -46.6920, 120, 'ATTENTION', 48, 60, '2026-04-20T12:00:00Z'),
  ('bin-005', 'Lixeira Moema', 'Zona Sul', -23.6035, -46.6614, 120, 'EMPTY', 90, 25, '2026-04-20T12:00:00Z'),
  ('bin-006', 'Lixeira Pinheiros', 'Zona Oeste', -23.5670, -46.7010, 120, 'FULL', 12, 90, '2026-04-20T12:00:00Z'),
  ('bin-007', 'Lixeira Santana', 'Zona Norte', -23.5056, -46.6253, 120, 'ATTENTION', 60, 50, '2026-04-20T12:00:00Z'),
  ('bin-008', 'Lixeira Tatuape', 'Zona Leste', -23.5402, -46.5762, 120, 'EMPTY', 78, 35, '2026-04-20T12:00:00Z'),
  ('bin-009', 'Lixeira Praca Central', 'Centro', -23.5505, -46.6333, 120, 'ATTENTION', 42, 65, '2026-04-20T12:00:00Z'),
  ('bin-010', 'Lixeira Terminal Norte', 'Zona Norte', -23.5401, -46.6202, 120, 'FULL', 24, 80, '2026-04-20T12:00:00Z')
on conflict (id) do update set
  name = excluded.name,
  region = excluded.region,
  latitude = excluded.latitude,
  longitude = excluded.longitude,
  bin_height_cm = excluded.bin_height_cm,
  status = excluded.status,
  current_distance_cm = excluded.current_distance_cm,
  current_fill_level_percent = excluded.current_fill_level_percent,
  last_update = excluded.last_update;

insert into public.sensor_readings (sensor_id, recorded_at, distance_cm, fill_level_percent)
values
  ('bin-001', '2026-04-20T06:00:00Z', 98.4, 18),
  ('bin-001', '2026-04-20T08:00:00Z', 96, 20),
  ('bin-001', '2026-04-20T10:00:00Z', 90, 25),
  ('bin-001', '2026-04-20T12:00:00Z', 84, 30),
  ('bin-002', '2026-04-20T06:00:00Z', 78, 35),
  ('bin-002', '2026-04-20T08:00:00Z', 72, 40),
  ('bin-002', '2026-04-20T10:00:00Z', 60, 50),
  ('bin-002', '2026-04-20T12:00:00Z', 54, 55),
  ('bin-003', '2026-04-20T06:00:00Z', 60, 50),
  ('bin-003', '2026-04-20T08:00:00Z', 48, 60),
  ('bin-003', '2026-04-20T10:00:00Z', 30, 75),
  ('bin-003', '2026-04-20T12:00:00Z', 18, 85),
  ('bin-004', '2026-04-20T06:00:00Z', 72, 40),
  ('bin-004', '2026-04-20T08:00:00Z', 66, 45),
  ('bin-004', '2026-04-20T10:00:00Z', 54, 55),
  ('bin-004', '2026-04-20T12:00:00Z', 48, 60),
  ('bin-005', '2026-04-20T06:00:00Z', 108, 10),
  ('bin-005', '2026-04-20T08:00:00Z', 102, 15),
  ('bin-005', '2026-04-20T10:00:00Z', 96, 20),
  ('bin-005', '2026-04-20T12:00:00Z', 90, 25),
  ('bin-006', '2026-04-20T06:00:00Z', 48, 60),
  ('bin-006', '2026-04-20T08:00:00Z', 36, 70),
  ('bin-006', '2026-04-20T10:00:00Z', 24, 80),
  ('bin-006', '2026-04-20T12:00:00Z', 12, 90),
  ('bin-007', '2026-04-20T06:00:00Z', 78, 35),
  ('bin-007', '2026-04-20T08:00:00Z', 72, 40),
  ('bin-007', '2026-04-20T10:00:00Z', 66, 45),
  ('bin-007', '2026-04-20T12:00:00Z', 60, 50),
  ('bin-008', '2026-04-20T06:00:00Z', 96, 20),
  ('bin-008', '2026-04-20T08:00:00Z', 90, 25),
  ('bin-008', '2026-04-20T10:00:00Z', 84, 30),
  ('bin-008', '2026-04-20T12:00:00Z', 78, 35),
  ('bin-009', '2026-04-20T06:00:00Z', 84, 30),
  ('bin-009', '2026-04-20T08:00:00Z', 78, 35),
  ('bin-009', '2026-04-20T10:00:00Z', 54, 55),
  ('bin-009', '2026-04-20T12:00:00Z', 42, 65),
  ('bin-010', '2026-04-20T06:00:00Z', 66, 45),
  ('bin-010', '2026-04-20T08:00:00Z', 60, 50),
  ('bin-010', '2026-04-20T10:00:00Z', 36, 70),
  ('bin-010', '2026-04-20T12:00:00Z', 24, 80)
on conflict (sensor_id, recorded_at) do update set
  distance_cm = excluded.distance_cm,
  fill_level_percent = excluded.fill_level_percent;

insert into public.collection_assignments (id, sensor_id, status, departure_time, estimated_collection_time, responsible_team, progress_percent, requested_by)
values ('collection-bin-006', 'bin-006', 'IN_PROGRESS', '2026-04-20T12:35:00Z', '2026-04-20T13:35:00Z', 'Equipe Oeste 02', 55, null)
on conflict (id) do update set
  sensor_id = excluded.sensor_id,
  status = excluded.status,
  departure_time = excluded.departure_time,
  estimated_collection_time = excluded.estimated_collection_time,
  responsible_team = excluded.responsible_team,
  progress_percent = excluded.progress_percent;
