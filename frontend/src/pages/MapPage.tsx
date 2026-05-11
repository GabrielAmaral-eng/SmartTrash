import { LocateFixed, Radar, Route } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { CircleMarker, MapContainer, Polyline, Popup, TileLayer, useMap } from 'react-leaflet';
import type { LatLngBoundsExpression } from 'leaflet';
import { SectionPanel } from '../components/ui/SectionPanel';
import { StatusBadge } from '../components/ui/StatusBadge';
import { fetchScheduledRoute, fetchSensorLocations } from '../services/api';
import type { BinStatus, ScheduledRoute, SensorLocation } from '../types/api';

const statusColors: Record<BinStatus, string> = {
  EMPTY: '#5ccafc',
  ATTENTION: '#fab0ff',
  FULL: '#ff716c',
};

const statusLabels: Record<BinStatus, string> = {
  EMPTY: 'Vazia',
  ATTENTION: 'Atencao',
  FULL: 'Cheia',
};

const paraisoCenter: [number, number] = [-23.5774, -46.6401];

export function MapPage() {
  const [locations, setLocations] = useState<SensorLocation[]>([]);
  const [scheduledRoute, setScheduledRoute] = useState<ScheduledRoute | null>(null);
  const [selectedId, setSelectedId] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([fetchSensorLocations(), fetchScheduledRoute()])
      .then(([locationsResponse, routeResponse]) => {
        setLocations(locationsResponse.locations);
        setScheduledRoute(routeResponse);
        setSelectedId(routeResponse.stops[0]?.id ?? locationsResponse.locations[0]?.id ?? '');
      })
      .catch(() => setError('Nao foi possivel carregar o mapa e a rota programada.'));
  }, []);

  const selected = locations.find((location) => location.id === selectedId) ?? locations[0];
  const routePositions = scheduledRoute?.stops.map((stop) => [stop.latitude, stop.longitude] as [number, number]) ?? [];
  const mapBounds = useMemo<LatLngBoundsExpression | null>(() => {
    const points = [
      ...locations.map((location) => [location.latitude, location.longitude] as [number, number]),
      ...routePositions,
    ];
    return points.length ? points : null;
  }, [locations, routePositions]);

  return (
    <div className="space-y-6">
      <header className="flex flex-col justify-between gap-4 md:flex-row md:items-end">
        <div>
          <h1 className="text-4xl font-black tracking-tight text-white">Mapa</h1>
          <p className="mt-2 text-sm text-muted">Mapa do Paraiso entre a Faculdade ESEG e o Colegio Etapa, com lixeiras proximas e rota programada.</p>
        </div>
        <div className="flex items-center gap-2 rounded-lg border border-white/5 bg-panel/70 px-4 py-3 text-xs font-bold uppercase tracking-[0.14em] text-muted">
          <LocateFixed size={16} className="text-secondary" />
          {locations.length} pontos ativos
        </div>
      </header>

      {error && <p className="rounded-lg bg-danger/10 p-4 text-danger">{error}</p>}

      <section className="grid gap-6 xl:grid-cols-12">
        <div className="xl:col-span-8">
          <div className="relative min-h-[620px] overflow-hidden rounded-lg border border-white/5 bg-black">
            <MapContainer center={paraisoCenter} zoom={16} scrollWheelZoom className="h-[620px] w-full">
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              {mapBounds && <FitMapBounds bounds={mapBounds} />}
              {routePositions.length > 1 && (
                <Polyline positions={routePositions} pathOptions={{ color: '#b6ff5c', opacity: 0.95, weight: 5 }} />
              )}
              {locations.map((location) => {
                const routeStop = scheduledRoute?.stops.find((stop) => stop.id === location.id);
                const isSelected = selected?.id === location.id;
                return (
                  <CircleMarker
                    key={location.id}
                    center={[location.latitude, location.longitude]}
                    pathOptions={{
                      color: isSelected ? '#fdfbfe' : statusColors[location.status],
                      fillColor: statusColors[location.status],
                      fillOpacity: isSelected ? 0.95 : 0.72,
                      opacity: 1,
                      weight: isSelected ? 4 : 2,
                    }}
                    radius={isSelected ? 13 : 9}
                    eventHandlers={{
                      click: () => setSelectedId(location.id),
                    }}
                  >
                    <Popup>
                      <div className="min-w-48">
                        <p className="text-xs font-bold uppercase tracking-wide text-slate-500">{location.id}</p>
                        <p className="mt-1 font-bold text-slate-950">{location.name}</p>
                        <p className="mt-2 text-sm text-slate-700">{location.fillLevelPercent}% de enchimento</p>
                        {routeStop && <p className="mt-1 text-sm font-bold text-slate-950">Parada #{routeStop.order} da rota 12h</p>}
                      </div>
                    </Popup>
                  </CircleMarker>
                );
              })}
            </MapContainer>

            <div className="pointer-events-none absolute bottom-5 left-5 z-[500] flex flex-wrap gap-3 rounded-lg border border-white/10 bg-panel/95 p-4 shadow-glow backdrop-blur">
              {(Object.keys(statusLabels) as BinStatus[]).map((status) => (
                <div key={status} className="flex items-center gap-2 text-xs font-bold text-muted">
                  <span className="h-3 w-3 rounded-full" style={{ backgroundColor: statusColors[status] }} />
                  {statusLabels[status]}
                </div>
              ))}
              <div className="flex items-center gap-2 text-xs font-bold text-muted">
                <span className="h-1 w-8 rounded-full bg-primary" />
                Rota 12h
              </div>
            </div>
          </div>
        </div>

        <aside className="space-y-4 xl:col-span-4">
          <SectionPanel title="Lixeira selecionada">
            {selected ? (
              <div>
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-xs font-bold uppercase tracking-[0.14em] text-muted">{selected.id}</p>
                    <h2 className="mt-2 text-2xl font-black text-white">{selected.name}</h2>
                  </div>
                  <StatusBadge status={selected.status} />
                </div>
                <div className="mt-6 grid grid-cols-2 gap-3">
                  <MapMetric label="Enchimento" value={`${selected.fillLevelPercent}%`} />
                  <MapMetric label="Latitude" value={selected.latitude.toFixed(4)} />
                  <MapMetric label="Longitude" value={selected.longitude.toFixed(4)} />
                  <MapMetric label="Status" value={statusLabels[selected.status]} />
                </div>
              </div>
            ) : (
              <p className="text-sm text-muted">Carregando pontos do mapa...</p>
            )}
          </SectionPanel>

          <SectionPanel title="Rota programada">
            {scheduledRoute ? (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-3">
                  <MapMetric label="Inicio" value={formatRouteTime(scheduledRoute.startTime)} />
                  <MapMetric label="Corte" value={`>${scheduledRoute.thresholdPercent}%`} />
                </div>
                <div className="rounded-lg border border-white/5 bg-black/30 p-4">
                  <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.14em] text-muted">
                    <Route size={15} className="text-primary" />
                    {scheduledRoute.responsibleTeam}
                  </div>
                  <p className="mt-3 text-sm text-white">{scheduledRoute.message}</p>
                </div>
                <div className="space-y-2">
                  {scheduledRoute.stops.map((stop) => (
                    <button
                      key={stop.id}
                      type="button"
                      onClick={() => setSelectedId(stop.id)}
                      className="flex w-full items-center justify-between rounded-lg bg-panel/70 p-3 text-left transition hover:bg-white/5"
                    >
                      <span className="flex min-w-0 items-center gap-3">
                        <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-md bg-primaryDim/15 text-xs font-black text-primary">
                          {stop.order}
                        </span>
                        <span className="truncate text-sm font-bold text-white">{stop.name}</span>
                      </span>
                      <span className="text-sm font-black text-primary">{stop.fillLevelPercent}%</span>
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              <p className="text-sm text-muted">Carregando rota diaria das 12h...</p>
            )}
          </SectionPanel>

          <SectionPanel title="Pontos monitorados">
            <div className="max-h-[360px] space-y-2 overflow-y-auto pr-1">
              {locations.map((location) => (
                <button
                  key={location.id}
                  type="button"
                  onClick={() => setSelectedId(location.id)}
                  className={`w-full rounded-lg border p-4 text-left transition ${
                    selected?.id === location.id
                      ? 'border-primary/40 bg-primaryDim/10'
                      : 'border-white/5 bg-black/30 hover:border-white/15 hover:bg-white/5'
                  }`}
                >
                  <div className="flex items-center justify-between gap-3">
                    <div className="flex min-w-0 items-center gap-3">
                      <Radar size={17} style={{ color: statusColors[location.status] }} />
                      <p className="truncate text-sm font-bold text-white">{location.name}</p>
                    </div>
                    <p className="text-sm font-black text-primary">{location.fillLevelPercent}%</p>
                  </div>
                  <p className="mt-2 text-xs text-muted">
                    {location.latitude.toFixed(4)}, {location.longitude.toFixed(4)}
                  </p>
                </button>
              ))}
            </div>
          </SectionPanel>
        </aside>
      </section>
    </div>
  );
}

function FitMapBounds({ bounds }: { bounds: LatLngBoundsExpression }) {
  const map = useMap();

  useEffect(() => {
    map.fitBounds(bounds, { padding: [42, 42], maxZoom: 17 });
  }, [bounds, map]);

  return null;
}

function MapMetric({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg bg-black/30 p-4">
      <p className="text-xs font-bold uppercase tracking-[0.14em] text-muted">{label}</p>
      <p className="mt-2 text-lg font-black text-white">{value}</p>
    </div>
  );
}

function formatRouteTime(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}
