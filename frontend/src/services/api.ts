import { supabase } from './supabase';
import type {
  CollectionAssignment,
  CollectionList,
  DashboardHistory,
  DashboardRegions,
  DashboardSummary,
  LoginRequest,
  LoginResponse,
  Profile,
  ScheduledRoute,
  SensorDetail,
  SensorHistory,
  SensorList,
  SensorLocations,
  UpdateUserRoleRequest,
  UserList,
} from '../types/api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'https://smarttrash-b7io.onrender.com';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const token = await currentAccessToken();
  const headers = new Headers(init?.headers);
  headers.set('Content-Type', 'application/json');
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers,
  });

  if (!response.ok) {
    throw new Error(`API request failed: ${response.status}`);
  }

  if (response.status === 204) {
    return null as T;
  }

  return response.json() as Promise<T>;
}

async function currentAccessToken() {
  const { data } = await supabase.auth.getSession();
  return data.session?.access_token;
}

export function login(payload: LoginRequest): Promise<LoginResponse> {
  return request<LoginResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function fetchCurrentProfile(): Promise<Profile | null> {
  return request<Profile | null>('/auth/profile');
}

export function fetchUsers(): Promise<UserList> {
  return request<UserList>('/auth/users');
}

export function updateUserRole(userId: string, payload: UpdateUserRoleRequest): Promise<Profile> {
  return request<Profile>(`/auth/users/${userId}/role`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });
}

export function fetchDashboardSummary(): Promise<DashboardSummary> {
  return request<DashboardSummary>('/dashboard/summary');
}

export function fetchDashboardHistory(): Promise<DashboardHistory> {
  return request<DashboardHistory>('/dashboard/history');
}

export function fetchDashboardRegions(): Promise<DashboardRegions> {
  return request<DashboardRegions>('/dashboard/regions');
}

export function fetchSensors(): Promise<SensorList> {
  return request<SensorList>('/sensors');
}

export function fetchSensor(id: string): Promise<SensorDetail> {
  return request<SensorDetail>(`/sensors/${id}`);
}

export function fetchSensorHistory(id: string): Promise<SensorHistory> {
  return request<SensorHistory>(`/sensors/${id}/history`);
}

export function fetchSensorLocations(): Promise<SensorLocations> {
  return request<SensorLocations>('/sensors/locations');
}

export function fetchCollections(): Promise<CollectionList> {
  return request<CollectionList>('/collections');
}

export function fetchScheduledRoute(): Promise<ScheduledRoute> {
  return request<ScheduledRoute>('/collections/scheduled-route');
}

export function allocateCollectionTeam(sensorId: string): Promise<CollectionAssignment> {
  return request<CollectionAssignment>(`/collections/allocations/${sensorId}`, {
    method: 'POST',
  });
}
