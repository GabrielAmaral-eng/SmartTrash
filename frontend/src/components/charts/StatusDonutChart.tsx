import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';
import type { BinStatus } from '../../types/api';

const colors: Record<BinStatus, string> = {
  EMPTY: '#5ccafc',
  ATTENTION: '#fab0ff',
  FULL: '#ff716c',
};

const labels: Record<BinStatus, string> = {
  EMPTY: 'Vazias',
  ATTENTION: 'Atenção',
  FULL: 'Cheias',
};

const legendFormatter = (value: string) => <span className="text-xs font-bold text-white">{value}</span>;

export function StatusDonutChart({ data }: { data: Record<BinStatus, number> }) {
  const chartData = (Object.keys(data) as BinStatus[]).map((status) => ({
    name: labels[status],
    value: data[status] ?? 0,
    status,
  }));

  return (
    <div className="h-72">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie data={chartData} dataKey="value" nameKey="name" innerRadius={65} outerRadius={95} paddingAngle={3}>
            {chartData.map((entry) => (
              <Cell key={entry.status} fill={colors[entry.status]} />
            ))}
          </Pie>
          <Tooltip />
          <Legend formatter={legendFormatter} iconType="circle" verticalAlign="bottom" wrapperStyle={{ paddingTop: 16 }} />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
