import type { BinStatus } from '../../types/api';

const labels: Record<BinStatus, string> = {
  EMPTY: 'Vazia',
  ATTENTION: 'Atenção',
  FULL: 'Cheia',
};

const classes: Record<BinStatus, string> = {
  EMPTY: 'bg-secondary/10 text-secondary border-secondary/20',
  ATTENTION: 'bg-warning/10 text-warning border-warning/20',
  FULL: 'bg-danger/10 text-danger border-danger/20',
};

export function StatusBadge({ status }: { status: BinStatus }) {
  return (
    <span className={`inline-flex rounded-full border px-3 py-1 text-xs font-bold ${classes[status]}`}>
      {labels[status]}
    </span>
  );
}
