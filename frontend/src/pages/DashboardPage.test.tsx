import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { DashboardPage } from './DashboardPage';

vi.mock('../services/api', () => ({
  fetchDashboardSummary: vi.fn().mockResolvedValue({ totalSensors: 8, byStatus: { EMPTY: 3, ATTENTION: 3, FULL: 2 }, averageFillLevelPercent: 56.5, totalAlerts: 5 }),
  fetchDashboardHistory: vi.fn().mockResolvedValue({ points: [{ timestamp: '2026-04-20T08:00:00Z', averageFillLevelPercent: 40 }] }),
  fetchDashboardRegions: vi.fn().mockResolvedValue({ regions: [{ region: 'Centro', sensorCount: 3, alertCount: 2, averageFillLevelPercent: 75 }] }),
}));

describe('DashboardPage', () => {
  it('renders KPIs and chart sections from API data', async () => {
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByText('8')).toBeInTheDocument());
    expect(screen.getByText('Status das lixeiras')).toBeInTheDocument();
    expect(screen.getByText('Resumo por região')).toBeInTheDocument();
  });
});
