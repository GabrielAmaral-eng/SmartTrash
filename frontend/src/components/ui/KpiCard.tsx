import type { LucideIcon } from 'lucide-react';

interface KpiCardProps {
  label: string;
  value: string | number;
  helper: string;
  icon: LucideIcon;
}

export function KpiCard({ label, value, helper, icon: Icon }: KpiCardProps) {
  return (
    <article className="rounded-lg border border-white/5 bg-panel/80 p-5 shadow-glow">
      <div className="flex items-center justify-between">
        <span className="text-xs font-bold uppercase tracking-[0.14em] text-muted">{label}</span>
        <Icon className="text-secondary" size={20} />
      </div>
      <div className="mt-7 flex items-end gap-2">
        <span className="text-4xl font-black tracking-tight text-white">{value}</span>
      </div>
      <p className="mt-4 border-t border-white/5 pt-4 text-xs leading-5 text-muted">{helper}</p>
    </article>
  );
}
