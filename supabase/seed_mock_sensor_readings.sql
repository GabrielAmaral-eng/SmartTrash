begin;

create temp table mock_sensor_patterns (
  sensor_id text primary key,
  reset_fill double precision not null,
  peak_fill double precision not null,
  cycle_hours double precision not null,
  phase_hours double precision not null,
  collection_window_hours double precision not null,
  wave double precision not null
) on commit drop;

insert into mock_sensor_patterns (sensor_id, reset_fill, peak_fill, cycle_hours, phase_hours, collection_window_hours, wave)
values
  ('bin-002', 18, 76, 84, 12, 8, 2.2),
  ('bin-003', 22, 92, 72, 70, 8, 2.8),
  ('bin-004', 16, 82, 96, 54, 8, 2.5),
  ('bin-005', 7, 58, 120, 10, 8, 1.6),
  ('bin-006', 24, 94, 68, 36, 8, 2.6),
  ('bin-007', 13, 71, 90, 28, 8, 2.0),
  ('bin-008', 8, 64, 108, 60, 8, 1.8),
  ('bin-009', 19, 88, 76, 22, 8, 2.7),
  ('bin-010', 21, 90, 80, 50, 8, 2.4);

create temp table mock_sensor_readings on commit drop as
with bounds as (
  select
    (date_trunc('month', timezone('America/Sao_Paulo', now())) at time zone 'America/Sao_Paulo') as month_start,
    now() - interval '10 minutes' as last_point
),
series as (
  select
    pattern.sensor_id,
    pattern.reset_fill,
    pattern.peak_fill,
    pattern.cycle_hours,
    pattern.phase_hours,
    pattern.collection_window_hours,
    pattern.wave,
    generated_at,
    (
      extract(epoch from (generated_at - bounds.month_start)) / 3600 + pattern.phase_hours
      - floor((extract(epoch from (generated_at - bounds.month_start)) / 3600 + pattern.phase_hours) / pattern.cycle_hours) * pattern.cycle_hours
    ) as cycle_position
  from mock_sensor_patterns pattern
  cross join bounds
  cross join lateral generate_series(bounds.month_start + interval '8 hours', bounds.last_point, interval '4 hours') generated_at
),
fills as (
  select
    series.sensor_id,
    series.generated_at as recorded_at,
    least(
      98::numeric,
      greatest(
        4::numeric,
        round(
          (
            case
              when series.cycle_position < series.collection_window_hours then
                series.reset_fill + (series.cycle_position / series.collection_window_hours) * 4
              else
                series.reset_fill
                + (series.peak_fill - series.reset_fill)
                * ((series.cycle_position - series.collection_window_hours) / (series.cycle_hours - series.collection_window_hours))
            end
            + series.wave * sin(extract(epoch from series.generated_at) / 172800 + series.phase_hours)
            + (series.wave / 2) * sin(extract(epoch from series.generated_at) / 21600)
          )::numeric,
          2
        )
      )
    ) as fill_level_percent
  from series
)
select
  fills.sensor_id,
  fills.recorded_at,
  round((bins.bin_height_cm * (100 - fills.fill_level_percent) / 100)::numeric, 2) as distance_cm,
  fills.fill_level_percent
from fills
join public.smart_bins bins on bins.id = fills.sensor_id;

insert into public.sensor_readings (sensor_id, recorded_at, distance_cm, fill_level_percent)
select sensor_id, recorded_at, distance_cm, fill_level_percent
from mock_sensor_readings
on conflict (sensor_id, recorded_at) do update set
  distance_cm = excluded.distance_cm,
  fill_level_percent = excluded.fill_level_percent;

with latest as (
  select distinct on (sensor_id)
    sensor_id,
    recorded_at,
    distance_cm,
    fill_level_percent
  from mock_sensor_readings
  order by sensor_id, recorded_at desc
)
update public.smart_bins bins
set
  current_distance_cm = latest.distance_cm,
  current_fill_level_percent = latest.fill_level_percent,
  status = case
    when latest.fill_level_percent < 50 then 'EMPTY'
    when latest.fill_level_percent < 80 then 'ATTENTION'
    else 'FULL'
  end,
  last_update = latest.recorded_at
from latest
where bins.id = latest.sensor_id;

commit;

with bounds as (
  select
    (date_trunc('day', timezone('America/Sao_Paulo', now())) at time zone 'America/Sao_Paulo') as day_start,
    (date_trunc('week', timezone('America/Sao_Paulo', now())) at time zone 'America/Sao_Paulo') as week_start,
    (date_trunc('month', timezone('America/Sao_Paulo', now())) at time zone 'America/Sao_Paulo') as month_start
),
ordered_readings as (
  select
    readings.sensor_id,
    readings.recorded_at,
    readings.fill_level_percent,
    lag(readings.fill_level_percent) over (
      partition by readings.sensor_id
      order by readings.recorded_at
    ) as previous_fill_level_percent
  from public.sensor_readings readings
  cross join bounds
  where readings.sensor_id in ('bin-002', 'bin-003', 'bin-004', 'bin-005', 'bin-006', 'bin-007', 'bin-008', 'bin-009', 'bin-010')
    and readings.recorded_at >= bounds.month_start
)
select
  ordered_readings.sensor_id,
  count(*) filter (where ordered_readings.recorded_at >= bounds.day_start) as today_points,
  count(*) filter (where ordered_readings.recorded_at >= bounds.week_start) as week_points,
  count(*) filter (where ordered_readings.recorded_at >= bounds.month_start) as month_points,
  count(*) filter (where ordered_readings.previous_fill_level_percent - ordered_readings.fill_level_percent >= 35) as collection_resets,
  min(ordered_readings.fill_level_percent) as min_fill,
  max(ordered_readings.fill_level_percent) as max_fill,
  max(ordered_readings.recorded_at) as latest_reading
from ordered_readings
cross join bounds
group by ordered_readings.sensor_id
order by ordered_readings.sensor_id;
