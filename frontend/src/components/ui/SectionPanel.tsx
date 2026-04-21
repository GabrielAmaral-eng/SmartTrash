import type { ReactNode } from 'react';

export function SectionPanel({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="rounded-lg border border-white/5 bg-panel/70 p-5">
      <h2 className="mb-5 text-lg font-bold tracking-tight text-white">{title}</h2>
      {children}
    </section>
  );
}
