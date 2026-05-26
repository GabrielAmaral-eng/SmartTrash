import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import type { SensorReading } from '../../types/api';

export function SensorHistoryChart({ data }: { data: SensorReading[] }) {
  const spansMultipleDays = data.some((point) => !isSameDay(point.timestamp, data[0]?.timestamp));
  const chartData = data.map((point) => ({
    time: formatChartTime(point.timestamp, spansMultipleDays),
    enchimento: point.fillLevelPercent,
  }));

  if (!data.length) {
    return (
      <div className="flex h-72 items-center justify-center rounded-lg border border-white/5 bg-black/20 text-sm font-semibold text-muted">
        Sem leituras nesse período.
      </div>
    );
  }

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

function formatChartTime(timestamp: string, includeDate: boolean) {
  return new Date(timestamp).toLocaleString('pt-BR', {
    day: includeDate ? '2-digit' : undefined,
    month: includeDate ? '2-digit' : undefined,
    hour: '2-digit',
    minute: '2-digit',
  });
}

function isSameDay(left?: string, right?: string) {
  if (!left || !right) {
    return true;
  }

  const leftDate = new Date(left);
  const rightDate = new Date(right);

  return (
    leftDate.getFullYear() === rightDate.getFullYear() &&
    leftDate.getMonth() === rightDate.getMonth() &&
    leftDate.getDate() === rightDate.getDate()
  );
}
