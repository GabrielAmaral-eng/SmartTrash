package com.smarttrash.repository;

import com.smarttrash.mock.MockSensorData;
import com.smarttrash.model.SmartBinSensor;
import com.smarttrash.service.BinStatusClassifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InMemorySensorRepository implements SensorRepository {

    private final List<SmartBinSensor> sensors;

    public InMemorySensorRepository(BinStatusClassifier classifier) {
        this.sensors = MockSensorData.createSensors(classifier);
    }

    @Override
    public List<SmartBinSensor> findAll() {
        return sensors;
    }

    @Override
    public Optional<SmartBinSensor> findById(String id) {
        return sensors.stream()
                .filter(sensor -> sensor.id().equals(id))
                .findFirst();
    }
}
