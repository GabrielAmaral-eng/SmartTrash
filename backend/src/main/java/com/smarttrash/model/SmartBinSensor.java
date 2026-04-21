package com.smarttrash.model;

import java.time.Instant;
import java.util.List;

public record SmartBinSensor(
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
        List<SensorReading> history
) {
}
