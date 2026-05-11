alter table public.profiles
  drop constraint if exists profiles_role_check;

alter table public.profiles
  add constraint profiles_role_check
  check (role in ('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'VIEWER'));

create or replace function private.current_user_role()
returns text
language sql
security definer
set search_path = ''
stable
as $$
  select role
  from public.profiles
  where id = auth.uid()
  limit 1
$$;

create or replace function private.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  insert into public.profiles (id, email, full_name, role)
  values (
    new.id,
    new.email,
    coalesce(new.raw_user_meta_data ->> 'full_name', split_part(new.email, '@', 1)),
    case
      when lower(new.email) = 'gabriel_41231@aluno.eseg.edu.br' then 'SUPER_ADMIN'
      else 'OPERATOR'
    end
  )
  on conflict (id) do update
    set email = excluded.email,
        full_name = coalesce(public.profiles.full_name, excluded.full_name),
        role = case
          when lower(excluded.email) = 'gabriel_41231@aluno.eseg.edu.br' then 'SUPER_ADMIN'
          else public.profiles.role
        end,
        updated_at = now();
  return new;
end;
$$;

update public.profiles
set role = 'SUPER_ADMIN'
where lower(email) = 'gabriel_41231@aluno.eseg.edu.br';

drop policy if exists profiles_select_own on public.profiles;
create policy profiles_select_authorized
  on public.profiles for select
  to authenticated
  using ((select auth.uid()) = id or private.current_user_role() = 'SUPER_ADMIN');

drop policy if exists profiles_update_super_admin on public.profiles;
create policy profiles_update_super_admin
  on public.profiles for update
  to authenticated
  using (private.current_user_role() = 'SUPER_ADMIN')
  with check (
    private.current_user_role() = 'SUPER_ADMIN'
    and (
      role <> 'SUPER_ADMIN'
      or lower(email) = 'gabriel_41231@aluno.eseg.edu.br'
    )
  );

grant select, update (role) on public.profiles to authenticated;

insert into public.smart_bins (id, name, region, latitude, longitude, bin_height_cm, status, current_distance_cm, current_fill_level_percent, last_update)
values
  ('bin-001', 'Lixeira ESEG - Entrada Rua Vergueiro', 'Paraiso', -23.5749, -46.6407, 120, 'EMPTY', 84, 30, '2026-04-20T12:00:00Z'),
  ('bin-002', 'Lixeira ESEG - Biblioteca', 'Paraiso', -23.5753, -46.6405, 120, 'ATTENTION', 54, 55, '2026-04-20T12:00:00Z'),
  ('bin-003', 'Lixeira Rua Vergueiro 1600', 'Paraiso', -23.5759, -46.6403, 120, 'FULL', 18, 85, '2026-04-20T12:00:00Z'),
  ('bin-004', 'Lixeira Rua Apeninos', 'Paraiso', -23.5766, -46.6412, 120, 'ATTENTION', 48, 60, '2026-04-20T12:00:00Z'),
  ('bin-005', 'Lixeira Praca Rodrigues de Abreu', 'Paraiso', -23.5772, -46.6401, 120, 'EMPTY', 90, 25, '2026-04-20T12:00:00Z'),
  ('bin-006', 'Lixeira Sistema Etapa', 'Vila Mariana', -23.5782, -46.6398, 120, 'FULL', 12, 90, '2026-04-20T12:00:00Z'),
  ('bin-007', 'Lixeira Rua Vergueiro 1900', 'Vila Mariana', -23.5788, -46.6396, 120, 'ATTENTION', 60, 50, '2026-04-20T12:00:00Z'),
  ('bin-008', 'Lixeira Colegio Etapa - Entrada', 'Vila Mariana', -23.5793, -46.6394, 120, 'EMPTY', 78, 35, '2026-04-20T12:00:00Z'),
  ('bin-009', 'Lixeira Colegio Etapa - Quadra', 'Vila Mariana', -23.5797, -46.6391, 120, 'ATTENTION', 42, 65, '2026-04-20T12:00:00Z'),
  ('bin-010', 'Lixeira Rua Topazio', 'Vila Mariana', -23.5803, -46.6388, 120, 'FULL', 24, 80, '2026-04-20T12:00:00Z')
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

update public.collection_assignments
set responsible_team = 'Equipe Programada Paraiso 12h'
where id = 'collection-bin-006';
