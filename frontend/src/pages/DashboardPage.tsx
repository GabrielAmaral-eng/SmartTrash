import { AlertTriangle, Gauge, Trash2, Waves } from 'lucide-react';
import { useEffect, useState } from 'react';
import { HistoryLineChart } from '../components/charts/HistoryLineChart';
import { RegionsBarChart } from '../components/charts/RegionsBarChart';
import { StatusDonutChart } from '../components/charts/StatusDonutChart';
import { KpiCard } from '../components/ui/KpiCard';
import { SectionPanel } from '../components/ui/SectionPanel';
import { fetchDashboardHistory, fetchDashboardRegions, fetchDashboardSummary } from '../services/api';
import type { DashboardHistory, DashboardRegions, DashboardSummary } from '../types/api';

export function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [history, setHistory] = useState<DashboardHistory | null>(null);
  const [regions, setRegions] = useState<DashboardRegions | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([fetchDashboardSummary(), fetchDashboardHistory(), fetchDashboardRegions()])
      .then(([summaryData, historyData, regionsData]) => {
        setSummary(summaryData);
        setHistory(historyData);
        setRegions(regionsData);
      })
      .catch(() => setError('Não foi possível carregar o dashboard. Confirme se o backend está em execução.'));
  }, []);

  if (error) {
    return <p className="rounded-lg bg-danger/10 p-4 text-danger">{error}</p>;
  }

  if (!summary || !history || !regions) {
    return <p className="text-muted">Carregando dashboard...</p>;
  }

  return (
    <div className="space-y-6">
      <header className="flex flex-col justify-between gap-4 md:flex-row md:items-end">
        <div>
          <h1 className="text-4xl font-black tracking-tight text-white">Dashboard</h1>
          <p className="mt-2 text-sm font-medium text-secondary">Dados operacionais carregados do Supabase.</p>
        </div>
      </header>

      <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
        <KpiCard label="Sensores" value={summary.totalSensors} helper="Lixeiras cadastradas no banco Supabase." icon={Trash2} />
        <KpiCard label="Ocupação média" value={`${summary.averageFillLevelPercent}%`} helper="Média calculada a partir das tabelas de sensores." icon={Gauge} />
        <KpiCard label="Alertas" value={summary.totalAlerts} helper="Soma de lixeiras em atenção e cheias." icon={AlertTriangle} />
        <KpiCard label="Regiões" value={regions.regions.length} helper="Localização cadastrada nos registros do Supabase." icon={Waves} />
      </div>

      <div className="grid gap-6 xl:grid-cols-12">
        <div className="xl:col-span-4">
          <SectionPanel title="Status das lixeiras">
            <StatusDonutChart data={summary.byStatus} />
          </SectionPanel>
        </div>
        <div className="xl:col-span-8">
          <SectionPanel title="Evolução da ocupação média">
            <HistoryLineChart data={history.points} />
          </SectionPanel>
        </div>
      </div>

      <SectionPanel title="Resumo por região">
        <RegionsBarChart data={regions.regions} />
      </SectionPanel>
    </div>
  );
}
