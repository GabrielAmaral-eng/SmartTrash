import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

const supabaseMocks = vi.hoisted(() => ({
  getSession: vi.fn(),
}));

vi.mock('./supabase', () => ({
  supabase: {
    auth: {
      getSession: supabaseMocks.getSession,
    },
  },
}));

import { allocateCollectionTeam, fetchCollections, fetchCurrentProfile, fetchDashboardSummary, fetchScheduledRoute, fetchSensors, fetchUsers, login, updateUserRole } from './api';

describe('api service', () => {
  beforeEach(() => {
    supabaseMocks.getSession.mockResolvedValue({ data: { session: null } });
  });

  afterEach(() => {
    vi.restoreAllMocks();
    supabaseMocks.getSession.mockReset();
  });

  it('posts login credentials to the backend auth endpoint', async () => {
    mockJsonResponse({ token: 'mock-token', user: { name: 'Operator', email: 'op@local', role: 'OPERATOR' } });

    const response = await login({ email: 'op@local', password: 'demo' });

    expect(response.token).toBe('mock-token');
    expect(fetch).toHaveBeenCalledWith(
      'https://smarttrash-b7io.onrender.com/auth/login',
      expect.objectContaining({ method: 'POST' }),
    );
  });

  it('sends the Supabase access token to backend requests', async () => {
    supabaseMocks.getSession.mockResolvedValue({ data: { session: { access_token: 'user-jwt' } } });
    mockJsonResponse({
      totalSensors: 2,
      byStatus: { EMPTY: 1, ATTENTION: 0, FULL: 1 },
      averageFillLevelPercent: 60,
      totalAlerts: 1,
    });

    await fetchDashboardSummary();

    const headers = fetchHeaders();
    expect(headers.get('Authorization')).toBe('Bearer user-jwt');
  });

  it('loads the current profile through the backend', async () => {
    mockJsonResponse({
      id: 'user-1',
      email: 'operator@smarttrash.local',
      fullName: 'Smart Trash Operator',
      role: 'OPERATOR',
    });

    const profile = await fetchCurrentProfile();

    expect(profile?.email).toBe('operator@smarttrash.local');
    expect(fetch).toHaveBeenCalledWith(
      'https://smarttrash-b7io.onrender.com/auth/profile',
      expect.any(Object),
    );
  });

  it('loads and updates users through the backend', async () => {
    mockJsonResponse({
      users: [
        {
          id: 'user-1',
          email: 'viewer@smarttrash.local',
          fullName: 'Viewer',
          role: 'VIEWER',
        },
      ],
    });

    const users = await fetchUsers();
    expect(users.users[0].role).toBe('VIEWER');

    mockJsonResponse({
      id: 'user-1',
      email: 'viewer@smarttrash.local',
      fullName: 'Viewer',
      role: 'OPERATOR',
    });

    const updated = await updateUserRole('user-1', { role: 'OPERATOR' });
    expect(updated.role).toBe('OPERATOR');
    expect(fetch).toHaveBeenLastCalledWith(
      'https://smarttrash-b7io.onrender.com/auth/users/user-1/role',
      expect.objectContaining({ method: 'PATCH' }),
    );
  });

  it('maps backend sensor list responses', async () => {
    mockJsonResponse({
      sensors: [
        {
          id: 'bin-001',
          name: 'Lixeira Centro',
          status: 'EMPTY',
          distanceCm: 84,
          fillLevelPercent: 30,
          region: 'Centro',
          lastUpdate: '2026-04-20T12:00:00Z',
        },
      ],
    });

    const response = await fetchSensors();

    expect(response.sensors[0]).toMatchObject({
      id: 'bin-001',
      name: 'Lixeira Centro',
      distanceCm: 84,
      fillLevelPercent: 30,
    });
  });

  it('maps backend collection list responses', async () => {
    mockJsonResponse({
      collections: [
        {
          id: 'collection-bin-002',
          sensorId: 'bin-002',
          sensorName: 'Lixeira Sul',
          region: 'Zona Sul',
          fillLevelPercent: 90,
          status: 'IN_PROGRESS',
          departureTime: '2026-04-20T12:35:00Z',
          estimatedCollectionTime: '2026-04-20T13:35:00Z',
          responsibleTeam: 'Equipe Sul 03',
          progressPercent: 55,
        },
      ],
    });

    const response = await fetchCollections();

    expect(response.collections[0]).toMatchObject({
      sensorId: 'bin-002',
      sensorName: 'Lixeira Sul',
      fillLevelPercent: 90,
    });
  });

  it('requests collection allocation from the backend', async () => {
    mockJsonResponse({
      id: 'collection-bin-002',
      sensorId: 'bin-002',
      sensorName: 'Lixeira Sul',
      region: 'Zona Sul',
      fillLevelPercent: 90,
      status: 'SCHEDULED',
      departureTime: '2026-04-20T12:35:00Z',
      estimatedCollectionTime: '2026-04-20T13:20:00Z',
      responsibleTeam: 'Equipe Sul 03',
      progressPercent: 12,
    });

    const response = await allocateCollectionTeam('bin-002');

    expect(response.sensorId).toBe('bin-002');
    expect(response.responsibleTeam).toBe('Equipe Sul 03');
    expect(fetch).toHaveBeenCalledWith(
      'https://smarttrash-b7io.onrender.com/collections/allocations/bin-002',
      expect.objectContaining({ method: 'POST' }),
    );
  });

  it('loads scheduled routes through the backend', async () => {
    mockJsonResponse({
      startTime: '2026-05-10T15:00:00Z',
      thresholdPercent: 50,
      responsibleTeam: 'Equipe Programada Paraiso 12h',
      active: true,
      message: 'Rota programada para lixeiras acima de 50% de preenchimento.',
      stops: [{ order: 1, id: 'bin-003', name: 'Lixeira Rua Vergueiro 1600', latitude: -23.5759, longitude: -46.6403, status: 'FULL', fillLevelPercent: 85 }],
    });

    const route = await fetchScheduledRoute();

    expect(route.stops[0]).toMatchObject({ id: 'bin-003', order: 1 });
    expect(fetch).toHaveBeenCalledWith(
      'https://smarttrash-b7io.onrender.com/collections/scheduled-route',
      expect.any(Object),
    );
  });
});

function mockJsonResponse(data: unknown) {
  vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
    new Response(JSON.stringify(data), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    }),
  );
}

function fetchHeaders() {
  const init = vi.mocked(fetch).mock.calls[0][1] as RequestInit;
  return init.headers as Headers;
}
