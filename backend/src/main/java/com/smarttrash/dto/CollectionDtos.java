package com.smarttrash.dto;

import com.smarttrash.model.CollectionStatus;

import java.time.Instant;
import java.util.List;

public final class CollectionDtos {
    private CollectionDtos() {
    }

    public record CollectionListResponse(List<CollectionAssignmentResponse> collections) {
    }

    public record CollectionAssignmentResponse(
            String id,
            String sensorId,
            String sensorName,
            String region,
            double fillLevelPercent,
            CollectionStatus status,
            Instant departureTime,
            Instant estimatedCollectionTime,
            String responsibleTeam,
            int progressPercent
    ) {
    }

    public record ScheduledRouteResponse(
            Instant startTime,
            double thresholdPercent,
            String responsibleTeam,
            boolean active,
            String message,
            List<ScheduledRouteStopResponse> stops
    ) {
    }

    public record ScheduledRouteStopResponse(
            int order,
            String id,
            String name,
            double latitude,
            double longitude,
            com.smarttrash.model.BinStatus status,
            double fillLevelPercent
    ) {
    }
}
