import { CalendarClock, CheckCircle2, Clock, Route, Truck, Users } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { KpiCard } from '../components/ui/KpiCard';
import { SectionPanel } from '../components/ui/SectionPanel';
import { fetchCollections } from '../services/api';
import type { CollectionAssignment, CollectionStatus } from '../types/api';

const statusLabels: Record<CollectionStatus, string> = {
  SCHEDULED: 'Agendada',
  IN_PROGRESS: 'Em rota',
  COLLECTED: 'Coletada',
};

const statusClasses: Record<CollectionStatus, string> = {
  SCHEDULED: 'border-secondary/20 bg-secondary/10 text-secondary',
  IN_PROGRESS: 'border-primary/20 bg-primaryDim/10 text-primary',
  COLLECTED: 'border-white/10 bg-white/10 text-white',
};

export function CollectionPage() {
  const [collections, setCollections] = useState<CollectionAssignment[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchCollections()
      .then((response) => setCollections(response.collections))
      .catch(() => setError('Não foi possível carregar as coletas. Confirme se o backend está em execução.'));
  }, []);

  const summary = useMemo(() => {
    return {
      total: collections.length,
      inRoute: collections.filter((collection) => collection.status === 'IN_PROGRESS').length,
      scheduled: collections.filter((collection) => collection.status === 'SCHEDULED').length,
      averageProgress: collections.length
        ? Math.round(collections.reduce((sum, collection) => sum + collection.progressPercent, 0) / collections.length)
        : 0,
    };
  }, [collections]);

  if (error) {
    return <p className="rounded-lg bg-danger/10 p-4 text-danger">{error}</p>;
  }

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-4xl font-black tracking-tight text-white">Coleta</h1>
        <p className="mt-2 text-sm text-muted">Acompanhamento das equipes alocadas para lixeiras acima do limite operacional.</p>
      </header>

      <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
        <KpiCard label="Coletas" value={summary.total} helper="Solicitações ativas persistidas no Supabase." icon={Truck} />
        <KpiCard label="Em rota" value={summary.inRoute} helper="Equipes já deslocadas para retirada." icon={Route} />
        <KpiCard label="Agendadas" value={summary.scheduled} helper="Coletas aguardando chegada ao ponto." icon={CalendarClock} />
        <KpiCard label="Progresso médio" value={`${summary.averageProgress}%`} helper="Média do andamento operacional." icon={CheckCircle2} />
      </div>

      <SectionPanel title="Progresso das coletas">
        {collections.length ? (
          <div className="grid gap-4 xl:grid-cols-2">
            {collections.map((collection) => (
              <article key={collection.id} className="rounded-lg border border-white/5 bg-black/30 p-5">
                <div className="flex flex-col justify-between gap-4 md:flex-row md:items-start">
                  <div>
                    <div className="flex flex-wrap items-center gap-2">
                      <span className={`rounded-lg border px-3 py-1 text-xs font-black uppercase tracking-[0.12em] ${statusClasses[collection.status]}`}>
                        {statusLabels[collection.status]}
                      </span>
                      <span className="text-xs font-bold uppercase tracking-[0.12em] text-muted">{collection.sensorId}</span>
                    </div>
                    <h2 className="mt-4 text-xl font-black text-white">{collection.sensorName}</h2>
                    <p className="mt-1 text-sm text-muted">
                      {collection.region} - {collection.fillLevelPercent}% de enchimento
                    </p>
                  </div>
                  <p className="text-3xl font-black text-primary">{collection.progressPercent}%</p>
                </div>

                <div className="mt-5 h-2 overflow-hidden rounded-full bg-panelHigh">
                  <div className="h-full rounded-full bg-primary" style={{ width: `${collection.progressPercent}%` }} />
                </div>

                <div className="mt-5 grid gap-3 md:grid-cols-3">
                  <CollectionMetric icon={Users} label="Equipe" value={collection.responsibleTeam} />
                  <CollectionMetric icon={Clock} label="Saída" value={formatTime(collection.departureTime)} />
                  <CollectionMetric icon={CalendarClock} label="Previsão" value={formatTime(collection.estimatedCollectionTime)} />
                </div>
              </article>
            ))}
          </div>
        ) : (
          <p className="rounded-lg bg-black/30 p-5 text-sm text-muted">Nenhuma equipe alocada no momento.</p>
        )}
      </SectionPanel>
    </div>
  );
}

function CollectionMetric({ icon: Icon, label, value }: { icon: typeof Users; label: string; value: string }) {
  return (
    <div className="rounded-lg bg-panel/70 p-4">
      <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.14em] text-muted">
        <Icon size={15} />
        {label}
      </div>
      <p className="mt-3 text-sm font-black text-white">{value}</p>
    </div>
  );
}

function formatTime(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit',
  }).format(new Date(value));
}
