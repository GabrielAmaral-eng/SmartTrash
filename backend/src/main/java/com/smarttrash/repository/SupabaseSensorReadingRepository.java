package com.smarttrash.repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smarttrash.model.BinStatus;
import com.smarttrash.model.SensorReadingCommand;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@ConditionalOnProperty(name = "smarttrash.data-source", havingValue = "supabase")
class SupabaseSensorReadingRepository implements SensorReadingRepository {

    private final SupabaseRestClient supabase;

    SupabaseSensorReadingRepository(SupabaseRestClient supabase) {
        this.supabase = supabase;
    }

    @Override
    public boolean sensorExists(String sensorId) {
        var rows = supabase.get()
                .uri(uri -> uri.path("/smart_bins")
                        .queryParam("select", "id")
                        .queryParam("id", "eq." + sensorId)
                        .queryParam("limit", "1")
                        .build())
                .headers(supabase.serviceHeaders())
                .retrieve()
                .body(new ParameterizedTypeReference<List<SensorIdRow>>() {});

        return rows != null && !rows.isEmpty();
    }

    @Override
    public void recordReading(SensorReadingCommand reading) {
        supabase.post()
                .uri(uri -> uri.path("/sensor_readings")
                        .queryParam("on_conflict", "sensor_id,recorded_at")
                        .build())
                .headers(supabase.serviceHeaders())
                .header("Prefer", "resolution=merge-duplicates")
                .body(new SensorReadingRow(
                        reading.sensorId(),
                        reading.timestamp(),
                        reading.distanceCm(),
                        reading.fillLevelPercent()
                ))
                .retrieve()
                .toBodilessEntity();

        supabase.patch()
                .uri(uri -> uri.path("/smart_bins")
                        .queryParam("id", "eq." + reading.sensorId())
                        .build())
                .headers(supabase.serviceHeaders())
                .body(SmartBinUpdate.from(reading))
                .retrieve()
                .toBodilessEntity();
    }

    private record SensorIdRow(String id) {
    }

    private record SensorReadingRow(
            @JsonProperty("sensor_id") String sensorId,
            @JsonProperty("recorded_at") Instant recordedAt,
            @JsonProperty("distance_cm") double distanceCm,
            @JsonProperty("fill_level_percent") double fillLevelPercent
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record SmartBinUpdate(
            BinStatus status,
            @JsonProperty("current_distance_cm") double currentDistanceCm,
            @JsonProperty("current_fill_level_percent") double currentFillLevelPercent,
            @JsonProperty("last_update") Instant lastUpdate,
            @JsonProperty("bin_height_cm") Double binHeightCm,
            Double latitude,
            Double longitude
    ) {
        static SmartBinUpdate from(SensorReadingCommand reading) {
            return new SmartBinUpdate(
                    reading.status(),
                    reading.distanceCm(),
                    reading.fillLevelPercent(),
                    reading.timestamp(),
                    reading.binHeightCm().orElse(null),
                    reading.latitude().orElse(null),
                    reading.longitude().orElse(null)
            );
        }
    }
}
