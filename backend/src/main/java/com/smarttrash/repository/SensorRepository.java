package com.smarttrash.repository;

import com.smarttrash.model.SmartBinSensor;

import java.util.List;
import java.util.Optional;

public interface SensorRepository {
    List<SmartBinSensor> findAll();

    Optional<SmartBinSensor> findById(String id);
}
