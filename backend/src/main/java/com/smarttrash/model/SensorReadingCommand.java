package com.smarttrash.model;

import java.time.Instant;
import java.util.Optional;

public record SensorReadingCommand(
        String sensorId,
        Instant timestamp,
        double distanceCm,
        double fillLevelPercent,
        BinStatus status,
        Optional<Double> binHeightCm,
        Optional<Double> latitude,
        Optional<Double> longitude,
        Optional<Integer> batteryPercent
) {
}
