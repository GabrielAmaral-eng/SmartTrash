package com.smarttrash.service;

import com.smarttrash.dto.SensorDtos.SensorDetailResponse;
import com.smarttrash.dto.SensorDtos.SensorHistoryResponse;
import com.smarttrash.dto.SensorDtos.SensorListResponse;
import com.smarttrash.dto.SensorDtos.SensorLocationResponse;
import com.smarttrash.dto.SensorDtos.SensorLocationsResponse;
import com.smarttrash.dto.SensorDtos.SensorReadingResponse;
import com.smarttrash.dto.SensorDtos.SensorSummaryResponse;
import com.smarttrash.exception.SensorNotFoundException;
import com.smarttrash.model.SmartBinSensor;
import com.smarttrash.repository.SensorRepository;
import org.springframework.stereotype.Service;

@Service
public class SensorService {

    private final SensorRepository repository;
    private final BinStatusClassifier classifier;

    public SensorService(SensorRepository repository, BinStatusClassifier classifier) {
        this.repository = repository;
        this.classifier = classifier;
    }

    public SensorListResponse listSensors() {
        return new SensorListResponse(repository.findAll().stream()
                .map(this::toSummary)
                .toList());
    }

    public SensorDetailResponse getSensor(String id) {
        return repository.findById(id)
                .map(this::toDetail)
                .orElseThrow(() -> new SensorNotFoundException(id));
    }

    public SensorHistoryResponse getSensorHistory(String id) {
        var sensor = repository.findById(id).orElseThrow(() -> new SensorNotFoundException(id));
        return new SensorHistoryResponse(sensor.id(), sensor.history().stream()
                .map(reading -> new SensorReadingResponse(reading.timestamp(), reading.distanceCm(), reading.fillLevelPercent()))
                .toList());
    }

    public SensorLocationsResponse getLocations() {
        return new SensorLocationsResponse(repository.findAll().stream()
                .map(sensor -> new SensorLocationResponse(
                        sensor.id(),
                        sensor.name(),
                        sensor.latitude(),
                        sensor.longitude(),
                        classifier.classify(sensor.fillLevelPercent()),
                        sensor.fillLevelPercent()
                ))
                .toList());
    }

    private SensorSummaryResponse toSummary(SmartBinSensor sensor) {
        return new SensorSummaryResponse(
                sensor.id(),
                sensor.name(),
                classifier.classify(sensor.fillLevelPercent()),
                sensor.distanceCm(),
                sensor.fillLevelPercent(),
                sensor.region(),
                sensor.lastUpdate()
        );
    }

    private SensorDetailResponse toDetail(SmartBinSensor sensor) {
        return new SensorDetailResponse(
                sensor.id(),
                sensor.name(),
                classifier.classify(sensor.fillLevelPercent()),
                sensor.distanceCm(),
                sensor.fillLevelPercent(),
                sensor.binHeightCm(),
                sensor.latitude(),
                sensor.longitude(),
                sensor.region(),
                sensor.lastUpdate(),
                sensor.history().stream()
                        .map(reading -> new SensorReadingResponse(reading.timestamp(), reading.distanceCm(), reading.fillLevelPercent()))
                        .toList()
        );
    }
}
