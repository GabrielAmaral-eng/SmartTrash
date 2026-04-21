package com.smarttrash.repository;

import com.smarttrash.model.CollectionAssignment;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository {
    List<CollectionAssignment> findAll();

    Optional<CollectionAssignment> findBySensorId(String sensorId);

    CollectionAssignment save(CollectionAssignment assignment);
}
