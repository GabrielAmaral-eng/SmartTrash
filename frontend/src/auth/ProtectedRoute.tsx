import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './AuthProvider';

export function ProtectedRoute() {
  const location = useLocation();
  const { loading, session } = useAuth();

  if (loading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-background px-5 text-sm font-semibold text-muted">
        Carregando sessao...
      </main>
    );
  }

  if (!session) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}
