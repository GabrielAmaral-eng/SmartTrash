package com.smarttrash.service;

import com.smarttrash.dto.CollectionDtos.CollectionAssignmentResponse;
import com.smarttrash.dto.CollectionDtos.CollectionListResponse;
import com.smarttrash.exception.SensorNotFoundException;
import com.smarttrash.model.CollectionAssignment;
import com.smarttrash.model.CollectionStatus;
import com.smarttrash.model.SmartBinSensor;
import com.smarttrash.repository.CollectionRepository;
import com.smarttrash.repository.SensorRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final SensorRepository sensorRepository;

    public CollectionService(CollectionRepository collectionRepository, SensorRepository sensorRepository) {
        this.collectionRepository = collectionRepository;
        this.sensorRepository = sensorRepository;
    }

    public CollectionListResponse listCollections() {
        return new CollectionListResponse(collectionRepository.findAll().stream()
                .map(this::toResponse)
                .toList());
    }

    public CollectionAssignmentResponse allocateTeam(String sensorId) {
        return collectionRepository.findBySensorId(sensorId)
                .map(this::toResponse)
                .orElseGet(() -> {
                    var sensor = sensorRepository.findById(sensorId)
                            .orElseThrow(() -> new SensorNotFoundException(sensorId));
                    if (sensor.fillLevelPercent() <= 70) {
                        throw new IllegalArgumentException("A lixeira precisa estar com mais de 70% de enchimento para alocar equipe.");
                    }
                    return toResponse(collectionRepository.save(newAssignment(sensor)));
                });
    }

    private CollectionAssignment newAssignment(SmartBinSensor sensor) {
        var departureTime = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        return new CollectionAssignment(
                "collection-" + sensor.id(),
                sensor.id(),
                sensor.name(),
                sensor.region(),
                sensor.fillLevelPercent(),
                CollectionStatus.SCHEDULED,
                departureTime,
                departureTime.plus(45, ChronoUnit.MINUTES),
                teamFor(sensor.region()),
                12
        );
    }

    private String teamFor(String region) {
        return switch (region) {
            case "Centro" -> "Equipe Centro 01";
            case "Zona Sul" -> "Equipe Sul 03";
            case "Zona Oeste" -> "Equipe Oeste 02";
            case "Zona Norte" -> "Equipe Norte 01";
            case "Zona Leste" -> "Equipe Leste 01";
            default -> "Equipe Operacional";
        };
    }

    private CollectionAssignmentResponse toResponse(CollectionAssignment assignment) {
        return new CollectionAssignmentResponse(
                assignment.id(),
                assignment.sensorId(),
                assignment.sensorName(),
                assignment.region(),
                assignment.fillLevelPercent(),
                assignment.status(),
                assignment.departureTime(),
                assignment.estimatedCollectionTime(),
                assignment.responsibleTeam(),
                assignment.progressPercent()
        );
    }
}
