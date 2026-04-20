package com.smarttrash.model;

import java.time.Instant;

public record SensorReading(
        Instant timestamp,
        double distanceCm,
        double fillLevelPercent
) {
}
