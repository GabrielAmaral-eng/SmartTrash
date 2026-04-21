import type {
  DashboardHistory,
  DashboardRegions,
  DashboardSummary,
<<<<<<< HEAD
  CollectionAssignment,
  CollectionList,
=======
>>>>>>> 8b2dcbe42d67f585adb5fa766588e67e21470521
  LoginRequest,
  LoginResponse,
  SensorDetail,
  SensorHistory,
  SensorList,
  SensorLocations,
} from '../types/api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...init?.headers,
    },
    ...init,
  });

  if (!response.ok) {
    throw new Error(`API request failed: ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export function login(payload: LoginRequest): Promise<LoginResponse> {
  return request<LoginResponse>('/auth/login', {
    method: 'POST',
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
<<<<<<< HEAD

export function fetchCollections(): Promise<CollectionList> {
  return request<CollectionList>('/collections');
}

export function allocateCollectionTeam(sensorId: string): Promise<CollectionAssignment> {
  return request<CollectionAssignment>(`/collections/allocations/${sensorId}`, {
    method: 'POST',
  });
}
=======
>>>>>>> 8b2dcbe42d67f585adb5fa766588e67e21470521
