package com.smarttrash.testsupport;

import com.smarttrash.model.SmartBinSensor;
import com.smarttrash.repository.SensorRepository;
import com.smarttrash.service.BinStatusClassifier;

import java.util.List;
import java.util.Optional;

public class TestSensorRepository implements SensorRepository {

    private final List<SmartBinSensor> sensors;

    public TestSensorRepository(BinStatusClassifier classifier) {
        this.sensors = TestSensorData.createSensors(classifier);
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
