package com.smarttrash.testsupport;

import com.smarttrash.model.BinStatus;
import com.smarttrash.service.BinStatusClassifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestSensorDataTest {

    @Test
    void testSensorsHaveCoherentDistanceFillAndStatusData() {
        var classifier = new BinStatusClassifier();
        var sensors = TestSensorData.createSensors(classifier);

        assertThat(sensors).hasSizeGreaterThanOrEqualTo(6);
        assertThat(sensors).allSatisfy(sensor -> {
            var expectedDistance = sensor.binHeightCm() * (100 - sensor.fillLevelPercent()) / 100;
            assertThat(sensor.distanceCm()).isCloseTo(expectedDistance, org.assertj.core.data.Offset.offset(0.01));
            assertThat(sensor.status()).isEqualTo(classifier.classify(sensor.fillLevelPercent()));
            assertThat(sensor.region()).isNotBlank();
            assertThat(sensor.history()).hasSizeGreaterThan(3);
        });
        assertThat(sensors).extracting("status").contains(BinStatus.EMPTY, BinStatus.ATTENTION, BinStatus.FULL);
    }
}
