package com.smarttrash.dto;

import com.smarttrash.model.BinStatus;

import java.time.Instant;
import java.util.List;

public final class SensorDtos {
    private SensorDtos() {
    }

    public record SensorListResponse(List<SensorSummaryResponse> sensors) {
    }

    public record SensorSummaryResponse(
            String id,
            String name,
            BinStatus status,
            double distanceCm,
            double fillLevelPercent,
            String region,
            Instant lastUpdate
    ) {
    }

    public record SensorDetailResponse(
            String id,
            String name,
            BinStatus status,
            double distanceCm,
            double fillLevelPercent,
            double binHeightCm,
            double latitude,
            double longitude,
            String region,
            Instant lastUpdate,
            List<SensorReadingResponse> history
    ) {
    }

    public record SensorHistoryResponse(
            String sensorId,
            List<SensorReadingResponse> points
    ) {
    }

    public record SensorReadingResponse(
            Instant timestamp,
            double distanceCm,
            double fillLevelPercent
    ) {
    }

    public record SensorLocationsResponse(List<SensorLocationResponse> locations) {
    }

    public record SensorLocationResponse(
            String id,
            String name,
            double latitude,
            double longitude,
            BinStatus status,
            double fillLevelPercent
    ) {
    }
}
