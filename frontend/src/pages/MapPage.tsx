import { Map, MapPin, Radar } from 'lucide-react';
import { useEffect, useState } from 'react';
import { SectionPanel } from '../components/ui/SectionPanel';
import { StatusBadge } from '../components/ui/StatusBadge';
import { fetchSensorLocations } from '../services/api';
import type { SensorLocation } from '../types/api';

export function MapPage() {
  const [locations, setLocations] = useState<SensorLocation[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchSensorLocations()
      .then((response) => setLocations(response.locations))
      .catch(() => setError('Não foi possível carregar as localizações mockadas.'));
  }, []);

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-4xl font-black tracking-tight text-white">Mapa</h1>
        <p className="mt-2 text-sm text-muted">Placeholder visual para a futura visualização geográfica.</p>
      </header>

      {error && <p className="rounded-lg bg-danger/10 p-4 text-danger">{error}</p>}

      <section className="relative min-h-[520px] overflow-hidden rounded-lg border border-white/5 bg-black">
        <div className="absolute inset-0 opacity-20 [background-image:linear-gradient(#47484a_1px,transparent_1px),linear-gradient(90deg,#47484a_1px,transparent_1px)] [background-size:80px_80px]" />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,rgba(0,112,235,0.18),#0d0e10_68%)]" />

        <div className="relative z-10 flex min-h-[520px] flex-col items-center justify-center px-6 text-center">
          <div className="mb-6 flex h-20 w-20 items-center justify-center rounded-full border border-primary/30 bg-primaryDim/10 text-primary shadow-glow">
            <Map size={36} />
          </div>
          <h2 className="text-3xl font-black tracking-tight text-white">Mapa em preparação</h2>
          <p className="mt-4 max-w-xl text-sm leading-6 text-muted">
            A API já fornece latitude, longitude, status e nível de enchimento. Nesta fase, a tela existe como placeholder e
            a visualização geográfica real será implementada futuramente.
          </p>
        </div>

        {locations.slice(0, 5).map((location, index) => (
          <div
            key={location.id}
            className="absolute z-20 hidden rounded-full bg-primaryDim p-2 text-white shadow-glow md:block"
            style={{ left: `${22 + index * 13}%`, top: `${28 + (index % 3) * 16}%` }}
            title={`${location.name} ${location.fillLevelPercent}%`}
          >
            <MapPin size={16} />
          </div>
        ))}
      </section>

      <SectionPanel title="Localizações mockadas disponíveis">
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
          {locations.map((location) => (
            <div key={location.id} className="rounded-lg bg-black/30 p-4">
              <div className="flex items-center justify-between gap-3">
                <Radar className="text-secondary" size={18} />
                <StatusBadge status={location.status} />
              </div>
              <p className="mt-4 font-bold text-white">{location.name}</p>
              <p className="mt-1 text-xs text-muted">
                {location.latitude.toFixed(4)}, {location.longitude.toFixed(4)}
              </p>
            </div>
          ))}
        </div>
      </SectionPanel>
    </div>
  );
}
