import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import type { SensorReading } from '../../types/api';

export function SensorHistoryChart({ data }: { data: SensorReading[] }) {
  const chartData = data.map((point) => ({
    time: new Date(point.timestamp).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }),
    enchimento: point.fillLevelPercent,
  }));

  return (
    <div className="h-72">
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart data={chartData}>
          <CartesianGrid stroke="rgba(255,255,255,0.08)" vertical={false} />
          <XAxis dataKey="time" stroke="#ababad" fontSize={12} />
          <YAxis stroke="#ababad" fontSize={12} domain={[0, 100]} />
          <Tooltip />
          <Area type="monotone" dataKey="enchimento" stroke="#fab0ff" fill="#fab0ff" fillOpacity={0.16} strokeWidth={3} />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}
