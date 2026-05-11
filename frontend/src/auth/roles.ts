import type { UserRole } from '../types/api';

export function hasAnyRole(role: UserRole | undefined, allowed: UserRole[]) {
  return Boolean(role && allowed.includes(role));
}

export function canAccessSensors(role: UserRole | undefined) {
  return hasAnyRole(role, ['SUPER_ADMIN', 'ADMIN', 'OPERATOR']);
}

export function canAccessCollections(role: UserRole | undefined) {
  return hasAnyRole(role, ['SUPER_ADMIN', 'ADMIN', 'OPERATOR']);
}

export function canManageUsers(role: UserRole | undefined) {
  return role === 'SUPER_ADMIN';
}
