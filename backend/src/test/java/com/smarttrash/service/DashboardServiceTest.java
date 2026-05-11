package com.smarttrash.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardServiceTest {

    private final DashboardService service = TestServices.dashboardService();

    @Test
    void buildsSummaryWithTotalsAverageAndAlerts() {
        var summary = service.getSummary();

        assertThat(summary.totalSensors()).isGreaterThanOrEqualTo(6);
        assertThat(summary.averageFillLevelPercent()).isBetween(0.0, 100.0);
        assertThat(summary.byStatus()).containsKeys("EMPTY", "ATTENTION", "FULL");
        assertThat(summary.totalAlerts()).isEqualTo(summary.byStatus().get("ATTENTION") + summary.byStatus().get("FULL"));
    }

    @Test
    void buildsAggregatedHistoryInChronologicalOrder() {
        var history = service.getHistory();

        assertThat(history.points()).hasSizeGreaterThan(3);
        assertThat(history.points()).isSortedAccordingTo((a, b) -> a.timestamp().compareTo(b.timestamp()));
        assertThat(history.points()).allSatisfy(point ->
                assertThat(point.averageFillLevelPercent()).isBetween(0.0, 100.0));
    }

    @Test
    void buildsRegionAggregation() {
        var regions = service.getRegions();

        assertThat(regions.regions()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(regions.regions()).allSatisfy(region -> {
            assertThat(region.region()).isNotBlank();
            assertThat(region.sensorCount()).isPositive();
            assertThat(region.averageFillLevelPercent()).isBetween(0.0, 100.0);
        });
    }
}
