package com.smarttrash.testsupport;

import com.smarttrash.model.SensorReadingCommand;
import com.smarttrash.repository.SensorReadingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestSensorReadingRepository implements SensorReadingRepository {

    private final Set<String> existingSensorIds;
    private final List<SensorReadingCommand> readings = new ArrayList<>();

    public TestSensorReadingRepository(Set<String> existingSensorIds) {
        this.existingSensorIds = existingSensorIds;
    }

    @Override
    public boolean sensorExists(String sensorId) {
        return existingSensorIds.contains(sensorId);
    }

    @Override
    public void recordReading(SensorReadingCommand reading) {
        readings.add(reading);
    }

    public List<SensorReadingCommand> readings() {
        return List.copyOf(readings);
    }
}
