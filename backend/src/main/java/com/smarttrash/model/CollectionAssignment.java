package com.smarttrash.model;

import java.time.Instant;

public record CollectionAssignment(
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
