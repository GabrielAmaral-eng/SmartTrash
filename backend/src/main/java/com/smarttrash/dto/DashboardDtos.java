package com.smarttrash.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class DashboardDtos {
    private DashboardDtos() {
    }

    public record DashboardSummaryResponse(
            int totalSensors,
            Map<String, Integer> byStatus,
            double averageFillLevelPercent,
            int totalAlerts
    ) {
    }

    public record DashboardHistoryResponse(
            List<DashboardHistoryPoint> points
    ) {
    }

    public record DashboardHistoryPoint(
            Instant timestamp,
            double averageFillLevelPercent
    ) {
    }

    public record DashboardRegionsResponse(
            List<RegionSummary> regions
    ) {
    }

    public record RegionSummary(
            String region,
            int sensorCount,
            int alertCount,
            double averageFillLevelPercent
    ) {
    }
}
