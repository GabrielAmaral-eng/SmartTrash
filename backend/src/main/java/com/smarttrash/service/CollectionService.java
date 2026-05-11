package com.smarttrash.service;

import com.smarttrash.dto.CollectionDtos.CollectionAssignmentResponse;
import com.smarttrash.dto.CollectionDtos.CollectionListResponse;
import com.smarttrash.dto.CollectionDtos.ScheduledRouteResponse;
import com.smarttrash.dto.CollectionDtos.ScheduledRouteStopResponse;
import com.smarttrash.exception.SensorNotFoundException;
import com.smarttrash.model.CollectionAssignment;
import com.smarttrash.model.CollectionStatus;
import com.smarttrash.model.SmartBinSensor;
import com.smarttrash.repository.CollectionRepository;
import com.smarttrash.repository.SensorRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
public class CollectionService {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final double SCHEDULED_ROUTE_THRESHOLD = 50;

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

    public ScheduledRouteResponse scheduledRoute() {
        var startTime = LocalDate.now(SAO_PAULO)
                .atTime(12, 0)
                .atZone(SAO_PAULO)
                .toInstant();
        var stops = sensorRepository.findAll().stream()
                .filter(sensor -> sensor.fillLevelPercent() > SCHEDULED_ROUTE_THRESHOLD)
                .sorted((left, right) -> Double.compare(right.fillLevelPercent(), left.fillLevelPercent()))
                .map(sensor -> new ScheduledRouteStopResponse(
                        0,
                        sensor.id(),
                        sensor.name(),
                        sensor.latitude(),
                        sensor.longitude(),
                        sensor.status(),
                        sensor.fillLevelPercent()
                ))
                .toList();
        var orderedStops = java.util.stream.IntStream.range(0, stops.size())
                .mapToObj(index -> {
                    var stop = stops.get(index);
                    return new ScheduledRouteStopResponse(
                            index + 1,
                            stop.id(),
                            stop.name(),
                            stop.latitude(),
                            stop.longitude(),
                            stop.status(),
                            stop.fillLevelPercent()
                    );
                })
                .toList();

        return new ScheduledRouteResponse(
                startTime,
                SCHEDULED_ROUTE_THRESHOLD,
                "Equipe Programada Paraiso 12h",
                !orderedStops.isEmpty(),
                orderedStops.isEmpty()
                        ? "Nenhuma lixeira acima de 50%. Rota programada sem recolhimento hoje."
                        : "Rota programada para lixeiras acima de 50% de preenchimento.",
                orderedStops
        );
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
