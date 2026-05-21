package com.smarttrash.dto;

import java.time.Instant;

public record SmartTrashMqttMessageDTO(
        String sensorId,
        Double distanceCm,
        Double fillLevelPercent,
        Double binHeightCm,
        Integer batteryPercent,
        Double latitude,
        Double longitude,
        Instant timestamp
) {
}
