import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthProvider';
import { hasAnyRole } from './roles';
import type { UserRole } from '../types/api';

export function RoleGate({ allowed, children }: { allowed: UserRole[]; children: JSX.Element }) {
  const { profile } = useAuth();

  if (!hasAnyRole(profile?.role, allowed)) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}
