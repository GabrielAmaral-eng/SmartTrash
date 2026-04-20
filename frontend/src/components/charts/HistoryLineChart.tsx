import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import type { DashboardHistoryPoint } from '../../types/api';

export function HistoryLineChart({ data }: { data: DashboardHistoryPoint[] }) {
  const chartData = data.map((point) => ({
    time: new Date(point.timestamp).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }),
    ocupacao: point.averageFillLevelPercent,
  }));

  return (
    <div className="h-80">
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart data={chartData}>
          <CartesianGrid stroke="rgba(255,255,255,0.08)" vertical={false} />
          <XAxis dataKey="time" stroke="#ababad" fontSize={12} />
          <YAxis stroke="#ababad" fontSize={12} domain={[0, 100]} />
          <Tooltip />
          <Area type="monotone" dataKey="ocupacao" stroke="#85adff" fill="#0070eb" fillOpacity={0.22} strokeWidth={3} />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}
