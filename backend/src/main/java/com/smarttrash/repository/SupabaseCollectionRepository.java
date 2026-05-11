package com.smarttrash.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smarttrash.model.CollectionAssignment;
import com.smarttrash.model.CollectionStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "smarttrash.data-source", havingValue = "supabase")
public class SupabaseCollectionRepository implements CollectionRepository {

    private static final String COLLECTION_SELECT = "id,sensor_id,status,departure_time,estimated_collection_time,responsible_team,progress_percent,smart_bins(name,region,current_fill_level_percent)";

    private final SupabaseRestClient supabase;

    public SupabaseCollectionRepository(SupabaseRestClient supabase) {
        this.supabase = supabase;
    }

    @Override
    public List<CollectionAssignment> findAll() {
        var rows = supabase.get()
                .uri(uri -> uri.path("/collection_assignments")
                        .queryParam("select", COLLECTION_SELECT)
                        .queryParam("order", "estimated_collection_time.asc")
                        .build())
                .headers(supabase.userHeaders())
                .retrieve()
                .body(new ParameterizedTypeReference<List<CollectionAssignmentRow>>() {});

        return (rows == null ? List.<CollectionAssignmentRow>of() : rows).stream()
                .map(CollectionAssignmentRow::toAssignment)
                .toList();
    }

    @Override
    public Optional<CollectionAssignment> findBySensorId(String sensorId) {
        var rows = supabase.get()
                .uri(uri -> uri.path("/collection_assignments")
                        .queryParam("select", COLLECTION_SELECT)
                        .queryParam("sensor_id", "eq." + sensorId)
                        .queryParam("limit", "1")
                        .build())
                .headers(supabase.userHeaders())
                .retrieve()
                .body(new ParameterizedTypeReference<List<CollectionAssignmentRow>>() {});

        if (rows == null || rows.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(rows.getFirst().toAssignment());
    }

    @Override
    public CollectionAssignment save(CollectionAssignment assignment) {
        supabase.post()
                .uri("/collection_assignments")
                .headers(supabase.userHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "id", assignment.id(),
                        "sensor_id", assignment.sensorId(),
                        "status", assignment.status().name(),
                        "departure_time", assignment.departureTime().toString(),
                        "estimated_collection_time", assignment.estimatedCollectionTime().toString(),
                        "responsible_team", assignment.responsibleTeam(),
                        "progress_percent", assignment.progressPercent()
                ))
                .retrieve()
                .toBodilessEntity();

        return findBySensorId(assignment.sensorId()).orElse(assignment);
    }

    private record CollectionAssignmentRow(
            String id,
            @JsonProperty("sensor_id") String sensorId,
            CollectionStatus status,
            @JsonProperty("departure_time") Instant departureTime,
            @JsonProperty("estimated_collection_time") Instant estimatedCollectionTime,
            @JsonProperty("responsible_team") String responsibleTeam,
            @JsonProperty("progress_percent") int progressPercent,
            @JsonProperty("smart_bins") SmartBinRow smartBin
    ) {
        CollectionAssignment toAssignment() {
            return new CollectionAssignment(
                    id,
                    sensorId,
                    smartBin == null ? sensorId : smartBin.name(),
                    smartBin == null ? "Sem regiao" : smartBin.region(),
                    smartBin == null ? 0 : smartBin.currentFillLevelPercent(),
                    status,
                    departureTime,
                    estimatedCollectionTime,
                    responsibleTeam,
                    progressPercent
            );
        }
    }

    private record SmartBinRow(
            String name,
            String region,
            @JsonProperty("current_fill_level_percent") double currentFillLevelPercent
    ) {
    }
}
