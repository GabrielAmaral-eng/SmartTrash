package com.smarttrash.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smarttrash.model.BinStatus;
import com.smarttrash.model.SensorReading;
import com.smarttrash.model.SmartBinSensor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "smarttrash.data-source", havingValue = "supabase")
public class SupabaseSensorRepository implements SensorRepository {

    private static final String SENSOR_SELECT = "id,name,region,status,current_distance_cm,current_fill_level_percent,bin_height_cm,latitude,longitude,last_update";
    private static final String READING_SELECT = "recorded_at,distance_cm,fill_level_percent";

    private final SupabaseRestClient supabase;

    public SupabaseSensorRepository(SupabaseRestClient supabase) {
        this.supabase = supabase;
    }

    @Override
    public List<SmartBinSensor> findAll() {
        var rows = supabase.get()
                .uri(uri -> uri.path("/smart_bins")
                        .queryParam("select", SENSOR_SELECT)
                        .queryParam("order", "id.asc")
                        .build())
                .headers(supabase.userHeaders())
                .retrieve()
                .body(new ParameterizedTypeReference<List<SmartBinRow>>() {});

        return (rows == null ? List.<SmartBinRow>of() : rows).stream()
                .map(row -> row.toSensor(List.of()))
                .toList();
    }

    @Override
    public Optional<SmartBinSensor> findById(String id) {
        var rows = supabase.get()
                .uri(uri -> uri.path("/smart_bins")
                        .queryParam("select", SENSOR_SELECT)
                        .queryParam("id", "eq." + id)
                        .queryParam("limit", "1")
                        .build())
                .headers(supabase.userHeaders())
                .retrieve()
                .body(new ParameterizedTypeReference<List<SmartBinRow>>() {});

        if (rows == null || rows.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(rows.getFirst().toSensor(findHistory(id)));
    }

    private List<SensorReading> findHistory(String sensorId) {
        var rows = supabase.get()
                .uri(uri -> uri.path("/sensor_readings")
                        .queryParam("select", READING_SELECT)
                        .queryParam("sensor_id", "eq." + sensorId)
                        .queryParam("order", "recorded_at.asc")
                        .build())
                .headers(supabase.userHeaders())
                .retrieve()
                .body(new ParameterizedTypeReference<List<SensorReadingRow>>() {});

        return (rows == null ? List.<SensorReadingRow>of() : rows).stream()
                .map(row -> new SensorReading(row.recordedAt(), row.distanceCm(), row.fillLevelPercent()))
                .toList();
    }

    private record SmartBinRow(
            String id,
            String name,
            String region,
            BinStatus status,
            @JsonProperty("current_distance_cm") double currentDistanceCm,
            @JsonProperty("current_fill_level_percent") double currentFillLevelPercent,
            @JsonProperty("bin_height_cm") double binHeightCm,
            double latitude,
            double longitude,
            @JsonProperty("last_update") Instant lastUpdate
    ) {
        SmartBinSensor toSensor(List<SensorReading> history) {
            return new SmartBinSensor(
                    id,
                    name,
                    status,
                    currentDistanceCm,
                    currentFillLevelPercent,
                    binHeightCm,
                    latitude,
                    longitude,
                    region,
                    lastUpdate,
                    history
            );
        }
    }

    private record SensorReadingRow(
            @JsonProperty("recorded_at") Instant recordedAt,
            @JsonProperty("distance_cm") double distanceCm,
            @JsonProperty("fill_level_percent") double fillLevelPercent
    ) {
    }
}
