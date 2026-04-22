import { BarChart3, LayoutDashboard, LogOut, Map, Trash2, Truck } from 'lucide-react';
import { NavLink, Outlet } from 'react-router-dom';

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/sensores', label: 'Sensores', icon: Trash2 },
  { to: '/coleta', label: 'Coleta', icon: Truck },
  { to: '/mapa', label: 'Mapa', icon: Map },
];

export function AppLayout() {
  return (
    <div className="min-h-screen bg-background text-white">
      <aside className="fixed left-0 top-0 z-40 hidden h-full w-64 flex-col bg-panelLow px-4 py-6 lg:flex">
        <div className="px-3 pt-2">
          <img src="/smart-trash-logo.png" alt="Smart Trash" className="h-auto w-44 object-contain" />
        </div>

        <nav className="mt-8 flex-1 space-y-2">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  [
                    'flex items-center gap-3 rounded-lg px-4 py-3 text-sm font-semibold transition',
                    isActive
                      ? 'border-r-2 border-primaryDim bg-primaryDim/10 text-primary'
                      : 'text-muted hover:bg-panel hover:text-white',
                  ].join(' ')
                }
              >
                <Icon size={18} />
                {item.label}
              </NavLink>
            );
          })}
        </nav>

        <div className="rounded-lg bg-panel p-4">
          <div className="flex items-center gap-2 text-[11px] font-bold uppercase tracking-[0.16em] text-muted">
            <BarChart3 size={14} />
            Fase atual
          </div>
          <p className="mt-3 text-sm text-white">Visualização mockada, sem ingestão real de sensores.</p>
        </div>
      </aside>

      <header className="fixed left-0 right-0 top-0 z-30 flex h-16 items-center justify-between bg-background/90 px-5 backdrop-blur lg:left-64 lg:px-8">
        <div>
          <p className="text-sm font-bold text-white">Painel de monitoramento</p>
          <p className="text-xs text-muted">Dados mockados sincronizados pela API</p>
        </div>
        <NavLink
          to="/login"
          className="flex h-10 w-10 items-center justify-center rounded-lg bg-panel text-muted transition hover:text-white"
          title="Sair"
        >
          <LogOut size={18} />
        </NavLink>
      </header>

      <main className="min-h-screen pt-20 lg:ml-64">
        <div className="mx-auto w-full max-w-7xl px-5 pb-10 lg:px-8">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
