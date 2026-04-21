package com.smarttrash.service;

import com.smarttrash.exception.SensorNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SensorServiceTest {

    private final SensorService service = TestServices.sensorService();

    @Test
    void listsSensorSummariesWithBackendCalculatedStatus() {
        var sensors = service.listSensors();

        assertThat(sensors.sensors()).hasSizeGreaterThanOrEqualTo(6);
        assertThat(sensors.sensors()).allSatisfy(sensor -> {
            assertThat(sensor.id()).isNotBlank();
            assertThat(sensor.status()).isNotNull();
            assertThat(sensor.fillLevelPercent()).isBetween(0.0, 100.0);
        });
    }

    @Test
    void returnsSensorDetailsAndHistory() {
        var first = service.listSensors().sensors().getFirst();
        var detail = service.getSensor(first.id());
        var history = service.getSensorHistory(first.id());

        assertThat(detail.id()).isEqualTo(first.id());
        assertThat(detail.latitude()).isNotZero();
        assertThat(detail.longitude()).isNotZero();
        assertThat(history.sensorId()).isEqualTo(first.id());
        assertThat(history.points()).hasSizeGreaterThan(3);
    }

    @Test
    void returnsLocationsForMapPlaceholder() {
        var locations = service.getLocations();

        assertThat(locations.locations()).hasSize(service.listSensors().sensors().size());
        assertThat(locations.locations()).allSatisfy(location -> {
            assertThat(location.latitude()).isNotZero();
            assertThat(location.longitude()).isNotZero();
        });
    }

    @Test
    void throwsWhenSensorDoesNotExist() {
        assertThatThrownBy(() -> service.getSensor("missing"))
                .isInstanceOf(SensorNotFoundException.class);
    }
}
