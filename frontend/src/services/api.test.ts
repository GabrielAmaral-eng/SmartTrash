import { afterEach, describe, expect, it, vi } from 'vitest';
<<<<<<< HEAD
import { allocateCollectionTeam, fetchCollections, fetchDashboardSummary, fetchSensors, login } from './api';
=======
import { fetchDashboardSummary, fetchSensors, login } from './api';
>>>>>>> 8b2dcbe42d67f585adb5fa766588e67e21470521

describe('api service', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('posts login credentials to the mock auth endpoint', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response(JSON.stringify({ token: 'mock-token', user: { name: 'Operator', email: 'op@local', role: 'OPERATOR' } }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    );

    const response = await login({ email: 'op@local', password: 'demo' });

    expect(response.token).toBe('mock-token');
    expect(fetch).toHaveBeenCalledWith('http://localhost:8080/auth/login', expect.objectContaining({ method: 'POST' }));
  });

  it('fetches dashboard summary through a structured endpoint', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response(JSON.stringify({ totalSensors: 8, byStatus: { EMPTY: 3 }, averageFillLevelPercent: 50, totalAlerts: 5 }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    );

    const summary = await fetchDashboardSummary();

    expect(summary.totalSensors).toBe(8);
  });

  it('fetches sensor summaries from the API', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response(JSON.stringify({ sensors: [{ id: 'bin-001', name: 'Centro', status: 'EMPTY', distanceCm: 70, fillLevelPercent: 30, region: 'Centro', lastUpdate: '2026-04-20T12:00:00Z' }] }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    );

    const response = await fetchSensors();

    expect(response.sensors[0].id).toBe('bin-001');
  });
<<<<<<< HEAD

  it('fetches collection assignments from the API', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response(JSON.stringify({ collections: [{ id: 'collection-bin-003', sensorId: 'bin-003', responsibleTeam: 'Equipe Sul 03', status: 'SCHEDULED', progressPercent: 12 }] }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    );

    const response = await fetchCollections();

    expect(response.collections[0].sensorId).toBe('bin-003');
    expect(fetch).toHaveBeenCalledWith('http://localhost:8080/collections', expect.any(Object));
  });

  it('posts a collection team allocation', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response(JSON.stringify({ id: 'collection-bin-003', sensorId: 'bin-003', responsibleTeam: 'Equipe Sul 03' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    );

    const response = await allocateCollectionTeam('bin-003');

    expect(response.sensorId).toBe('bin-003');
    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8080/collections/allocations/bin-003',
      expect.objectContaining({ method: 'POST' }),
    );
  });
=======
>>>>>>> 8b2dcbe42d67f585adb5fa766588e67e21470521
});
