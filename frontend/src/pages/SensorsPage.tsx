import { CheckCircle2, MapPin, Ruler, Trash2, Truck } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { SensorHistoryChart } from '../components/charts/SensorHistoryChart';
import { SectionPanel } from '../components/ui/SectionPanel';
import { StatusBadge } from '../components/ui/StatusBadge';
import { allocateCollectionTeam, fetchCollections, fetchSensor, fetchSensorHistory, fetchSensors } from '../services/api';
import type { SensorDetail, SensorHistory, SensorReading, SensorSummary } from '../types/api';

type HistoryRange = 'today' | 'week' | 'month';

const historyRangeOptions: Array<{ value: HistoryRange; label: string }> = [
  { value: 'today', label: 'Hoje' },
  { value: 'week', label: 'Nesta semana' },
  { value: 'month', label: 'Neste mês' },
];

export function SensorsPage() {
  const navigate = useNavigate();
  const [sensors, setSensors] = useState<SensorSummary[]>([]);
  const [selectedId, setSelectedId] = useState<string>('');
  const [detail, setDetail] = useState<SensorDetail | null>(null);
  const [history, setHistory] = useState<SensorHistory | null>(null);
  const [historyRange, setHistoryRange] = useState<HistoryRange>('today');
  const [allocatedSensorIds, setAllocatedSensorIds] = useState<string[]>([]);
  const [allocating, setAllocating] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchSensors()
      .then((response) => {
        setSensors(response.sensors);
        setSelectedId(response.sensors[0]?.id ?? '');
      })
      .catch(() => setError('Não foi possível carregar sensores.'));
  }, []);

  useEffect(() => {
    fetchCollections()
      .then((response) => setAllocatedSensorIds(response.collections.map((collection) => collection.sensorId)))
      .catch(() => undefined);
  }, []);

  useEffect(() => {
    if (!selectedId) {
      return;
    }
    Promise.all([fetchSensor(selectedId), fetchSensorHistory(selectedId)])
      .then(([detailData, historyData]) => {
        setDetail(detailData);
        setHistory(historyData);
      })
      .catch(() => setError('Não foi possível carregar os detalhes do sensor.'));
  }, [selectedId]);

  const filteredHistory = useMemo(() => {
    return filterHistory(history?.points ?? [], historyRange);
  }, [history?.points, historyRange]);

  function allocateTeam() {
    if (!detail || detail.fillLevelPercent <= 70 || allocating) {
      return;
    }
    setAllocating(true);
    allocateCollectionTeam(detail.id)
      .then((assignment) => {
        setAllocatedSensorIds((current) => Array.from(new Set([...current, assignment.sensorId])));
        navigate('/coleta');
      })
      .catch(() => setError('Não foi possível alocar uma equipe para esta lixeira.'))
      .finally(() => setAllocating(false));
  }

  if (error) {
    return <p className="rounded-lg bg-danger/10 p-4 text-danger">{error}</p>;
  }

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-4xl font-black tracking-tight text-white">Sensores</h1>
      </header>

      <div className="grid gap-6 xl:grid-cols-12">
        <section className="rounded-lg border border-white/5 bg-panel/70 p-5 xl:col-span-5">
          <h2 className="mb-5 text-lg font-bold">Rede de lixeiras</h2>
          <div className="overflow-hidden rounded-lg border border-white/5">
            <table className="w-full text-left text-sm">
              <thead className="bg-black/40 text-xs uppercase tracking-[0.12em] text-muted">
                <tr>
                  <th className="px-4 py-3">Nome</th>
                  <th className="px-4 py-3">Região</th>
                  <th className="px-4 py-3">Status</th>
                </tr>
              </thead>
              <tbody>
                {sensors.map((sensor) => (
                  <tr
                    key={sensor.id}
                    className={`cursor-pointer border-t border-white/5 transition hover:bg-white/5 ${selectedId === sensor.id ? 'bg-primaryDim/10' : ''}`}
                    onClick={() => setSelectedId(sensor.id)}
                  >
                    <td className="px-4 py-3 font-semibold text-white">{sensor.name}</td>
                    <td className="px-4 py-3 text-muted">{sensor.region}</td>
                    <td className="px-4 py-3">
                      <StatusBadge status={sensor.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section className="space-y-6 xl:col-span-7">
          {detail ? (
            <>
              <div className="rounded-lg border border-white/5 bg-panel/70 p-6">
                <div className="flex flex-col justify-between gap-4 md:flex-row md:items-start">
                  <div>
                    <p className="text-xs font-bold uppercase tracking-[0.16em] text-muted">{detail.id}</p>
                    <h2 className="mt-2 text-2xl font-black text-white">{detail.name}</h2>
                    <div className="mt-4">
                      <StatusBadge status={detail.status} />
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-5xl font-black text-primary">{detail.fillLevelPercent}%</p>
                    <p className="text-xs uppercase tracking-[0.14em] text-muted">enchimento</p>
                  </div>
                </div>

                {detail.fillLevelPercent > 70 && (
                  <div className="mt-6 flex flex-col gap-3 rounded-lg border border-primary/20 bg-primaryDim/10 p-4 md:flex-row md:items-center md:justify-between">
                    <div>
                      <p className="text-sm font-bold text-white">Coleta recomendada</p>
                    </div>
                    <button
                      type="button"
                      onClick={allocateTeam}
                      disabled={allocatedSensorIds.includes(detail.id) || allocating}
                      className="inline-flex items-center justify-center gap-2 rounded-lg bg-primary px-4 py-3 text-sm font-black text-background transition hover:bg-white disabled:cursor-not-allowed disabled:bg-panelHigh disabled:text-muted"
                    >
                      {allocatedSensorIds.includes(detail.id) ? <CheckCircle2 size={18} /> : <Truck size={18} />}
                      {allocatedSensorIds.includes(detail.id) ? 'Equipe alocada' : allocating ? 'Alocando...' : 'Alocar equipe de coleta'}
                    </button>
                  </div>
                )}

                <div className="mt-8 grid gap-4 md:grid-cols-3">
                  <Metric icon={Ruler} label="Distância" value={`${detail.distanceCm} cm`} />
                  <Metric icon={Trash2} label="Altura da lixeira" value={`${detail.binHeightCm} cm`} />
                  <Metric icon={MapPin} label="Região" value={detail.region} />
                </div>
              </div>

              <SectionPanel title="Histórico de enchimento">
                <div className="mb-5 inline-flex max-w-full rounded-lg border border-white/5 bg-black p-1">
                  {historyRangeOptions.map((option) => (
                    <button
                      key={option.value}
                      type="button"
                      aria-pressed={historyRange === option.value}
                      onClick={() => setHistoryRange(option.value)}
                      className={`rounded-md px-4 py-2 text-sm font-black transition ${
                        historyRange === option.value ? 'bg-primary text-background' : 'text-muted hover:text-white'
                      }`}
                    >
                      {option.label}
                    </button>
                  ))}
                </div>
                <SensorHistoryChart data={filteredHistory} />
              </SectionPanel>
            </>
          ) : (
            <p className="text-muted">Selecione uma lixeira para ver os detalhes.</p>
          )}
        </section>
      </div>
    </div>
  );
}

function filterHistory(points: SensorReading[], range: HistoryRange) {
  const now = new Date();
  const start = getRangeStart(now, range);
  const nowTime = now.getTime();
  const startTime = start.getTime();

  return points.filter((point) => {
    const timestamp = new Date(point.timestamp).getTime();
    return timestamp >= startTime && timestamp <= nowTime;
  });
}

function getRangeStart(date: Date, range: HistoryRange) {
  if (range === 'month') {
    return new Date(date.getFullYear(), date.getMonth(), 1);
  }

  const start = startOfDay(date);
  if (range === 'week') {
    const daysFromMonday = (start.getDay() + 6) % 7;
    start.setDate(start.getDate() - daysFromMonday);
  }

  return start;
}

function startOfDay(date: Date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function Metric({ icon: Icon, label, value }: { icon: LucideIcon; label: string; value: string }) {
  return (
    <div className="rounded-lg bg-black/30 p-4">
      <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.14em] text-muted">
        <Icon size={15} />
        {label}
      </div>
      <p className="mt-3 text-xl font-black text-white">{value}</p>
    </div>
  );
}
