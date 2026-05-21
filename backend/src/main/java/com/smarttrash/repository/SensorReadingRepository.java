package com.smarttrash.repository;

import com.smarttrash.model.SensorReadingCommand;

public interface SensorReadingRepository {
    boolean sensorExists(String sensorId);

    void recordReading(SensorReadingCommand reading);
}
