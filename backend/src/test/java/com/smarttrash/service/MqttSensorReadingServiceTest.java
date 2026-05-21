package com.smarttrash.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttrash.model.BinStatus;
import com.smarttrash.testsupport.TestSensorReadingRepository;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.smarttrash.service.MqttSensorReadingService.ProcessingResult.INVALID_JSON;
import static com.smarttrash.service.MqttSensorReadingService.ProcessingResult.INVALID_MESSAGE;
import static com.smarttrash.service.MqttSensorReadingService.ProcessingResult.PROCESSED;
import static com.smarttrash.service.MqttSensorReadingService.ProcessingResult.SENSOR_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

class MqttSensorReadingServiceTest {

    private final TestSensorReadingRepository repository = new TestSensorReadingRepository(Set.of("bin-001"));
    private final MqttSensorReadingService service = new MqttSensorReadingService(
            new ObjectMapper().findAndRegisterModules(),
            repository,
            new BinStatusClassifier()
    );

    @Test
    void processesValidJsonAndStoresReading() {
        var result = service.processPayload("""
                {
                  "sensorId": "bin-001",
                  "distanceCm": 42.0,
                  "fillLevelPercent": 65.0,
                  "binHeightCm": 120.0,
                  "batteryPercent": 87,
                  "latitude": -23.5749,
                  "longitude": -46.6407,
                  "timestamp": "2026-05-13T20:30:00Z"
                }
                """);

        assertThat(result).isEqualTo(PROCESSED);
        assertThat(repository.readings()).singleElement().satisfies(reading -> {
            assertThat(reading.sensorId()).isEqualTo("bin-001");
            assertThat(reading.distanceCm()).isEqualTo(42.0);
            assertThat(reading.fillLevelPercent()).isEqualTo(65.0);
            assertThat(reading.status()).isEqualTo(BinStatus.ATTENTION);
            assertThat(reading.binHeightCm()).contains(120.0);
            assertThat(reading.batteryPercent()).contains(87);
        });
    }

    @Test
    void rejectsInvalidJson() {
        var result = service.processPayload("{");

        assertThat(result).isEqualTo(INVALID_JSON);
        assertThat(repository.readings()).isEmpty();
    }

    @Test
    void rejectsMissingRequiredFields() {
        var result = service.processPayload("""
                {
                  "sensorId": "bin-001",
                  "distanceCm": 42.0
                }
                """);

        assertThat(result).isEqualTo(INVALID_MESSAGE);
        assertThat(repository.readings()).isEmpty();
    }

    @Test
    void rejectsUnknownSensor() {
        var result = service.processPayload("""
                {
                  "sensorId": "missing",
                  "distanceCm": 42.0,
                  "fillLevelPercent": 65.0,
                  "timestamp": "2026-05-13T20:30:00Z"
                }
                """);

        assertThat(result).isEqualTo(SENSOR_NOT_FOUND);
        assertThat(repository.readings()).isEmpty();
    }

    @Test
    void calculatesFullStatusFromFillLevel() {
        var result = service.processPayload("""
                {
                  "sensorId": "bin-001",
                  "distanceCm": 10.0,
                  "fillLevelPercent": 90.0,
                  "timestamp": "2026-05-13T20:30:00Z"
                }
                """);

        assertThat(result).isEqualTo(PROCESSED);
        assertThat(repository.readings()).singleElement()
                .extracting(reading -> reading.status())
                .isEqualTo(BinStatus.FULL);
    }
}
