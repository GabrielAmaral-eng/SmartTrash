import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import type { RegionSummary } from '../../types/api';

export function RegionsBarChart({ data }: { data: RegionSummary[] }) {
  return (
    <div className="h-72">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data}>
          <CartesianGrid stroke="rgba(255,255,255,0.08)" vertical={false} />
          <XAxis dataKey="region" stroke="#ababad" fontSize={12} />
          <YAxis stroke="#ababad" fontSize={12} />
          <Tooltip />
          <Bar dataKey="sensorCount" name="Lixeiras" fill="#5ccafc" radius={[6, 6, 0, 0]} />
          <Bar dataKey="alertCount" name="Alertas" fill="#ff716c" radius={[6, 6, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
