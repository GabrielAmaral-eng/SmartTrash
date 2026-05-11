package com.smarttrash.testsupport;

import com.smarttrash.model.CollectionAssignment;
import com.smarttrash.model.CollectionStatus;
import com.smarttrash.model.SmartBinSensor;
import com.smarttrash.repository.CollectionRepository;
import com.smarttrash.repository.SensorRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TestCollectionRepository implements CollectionRepository {

    private static final Instant BASE_TIME = Instant.parse("2026-04-20T13:00:00Z");

    private final ConcurrentHashMap<String, CollectionAssignment> assignments = new ConcurrentHashMap<>();

    public TestCollectionRepository(SensorRepository sensorRepository) {
        sensorRepository.findById("bin-006").ifPresent(sensor -> save(seedAssignment(sensor)));
    }

    @Override
    public List<CollectionAssignment> findAll() {
        var collections = new ArrayList<>(assignments.values());
        collections.sort(Comparator.comparing(CollectionAssignment::estimatedCollectionTime));
        return List.copyOf(collections);
    }

    @Override
    public Optional<CollectionAssignment> findBySensorId(String sensorId) {
        return Optional.ofNullable(assignments.get(sensorId));
    }

    @Override
    public CollectionAssignment save(CollectionAssignment assignment) {
        assignments.put(assignment.sensorId(), assignment);
        return assignment;
    }

    private CollectionAssignment seedAssignment(SmartBinSensor sensor) {
        return new CollectionAssignment(
                "collection-" + sensor.id(),
                sensor.id(),
                sensor.name(),
                sensor.region(),
                sensor.fillLevelPercent(),
                CollectionStatus.IN_PROGRESS,
                BASE_TIME.minus(25, ChronoUnit.MINUTES),
                BASE_TIME.plus(35, ChronoUnit.MINUTES),
                "Equipe Programada Paraiso 12h",
                55
        );
    }
}
