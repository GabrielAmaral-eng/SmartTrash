package com.smarttrash.mock;

import com.smarttrash.model.SmartBinSensor;
import com.smarttrash.model.SensorReading;
import com.smarttrash.service.BinStatusClassifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public final class MockSensorData {

    private static final Instant BASE_TIME = Instant.parse("2026-04-20T12:00:00Z");

    private MockSensorData() {
    }

    public static List<SmartBinSensor> createSensors(BinStatusClassifier classifier) {
        return List.of(
                sensor(classifier, "bin-001", "Lixeira Av. Paulista", "Centro", -23.5614, -46.6559, 120, List.of(20.0, 25.0, 30.0)),
                sensor(classifier, "bin-002", "Lixeira Rua Augusta", "Centro", -23.5558, -46.6581, 120, List.of(40.0, 50.0, 55.0)),
                sensor(classifier, "bin-003", "Lixeira Parque Ibirapuera", "Zona Sul", -23.5874, -46.6576, 120, List.of(60.0, 75.0, 85.0)),
                sensor(classifier, "bin-004", "Lixeira Vila Madalena", "Zona Oeste", -23.5503, -46.6920, 120, List.of(45.0, 55.0, 60.0)),
                sensor(classifier, "bin-005", "Lixeira Moema", "Zona Sul", -23.6035, -46.6614, 120, List.of(15.0, 20.0, 25.0)),
                sensor(classifier, "bin-006", "Lixeira Pinheiros", "Zona Oeste", -23.5670, -46.7010, 120, List.of(70.0, 80.0, 90.0)),
                sensor(classifier, "bin-007", "Lixeira Santana", "Zona Norte", -23.5056, -46.6253, 120, List.of(40.0, 45.0, 50.0)),
                sensor(classifier, "bin-008", "Lixeira Tatuapé", "Zona Leste", -23.5402, -46.5762, 120, List.of(25.0, 30.0, 35.0)),
                sensor(classifier, "bin-009", "Lixeira Praça Central", "Centro", -23.5505, -46.6333, 120, List.of(35.0, 55.0, 65.0)),
                sensor(classifier, "bin-010", "Lixeira Terminal Norte", "Zona Norte", -23.5401, -46.6202, 120, List.of(50.0, 70.0, 80.0))
        );
    }

    private static SmartBinSensor sensor(
            BinStatusClassifier classifier,
            String id,
            String name,
            String region,
            double latitude,
            double longitude,
            double binHeightCm,
        List<Double> fills
    ) {
        var readings = readings(binHeightCm, fills);
        var latest = readings.getLast();
        return new SmartBinSensor(
                id,
                name,
                classifier.classify(latest.fillLevelPercent()),
                latest.distanceCm(),
                latest.fillLevelPercent(),
                binHeightCm,
                latitude,
                longitude,
                region,
                latest.timestamp(),
                readings
        );
    }

    private static List<SensorReading> readings(double binHeightCm, List<Double> fills) {
        var readings = new ArrayList<SensorReading>();
        for (var index = 0; index < fills.size(); index++) {
            var fill = fills.get(index);
            var hoursBeforeBase = (long) (fills.size() - 1 - index) * 2;
            readings.add(new SensorReading(
                    BASE_TIME.minus(hoursBeforeBase, ChronoUnit.HOURS),
                    distance(binHeightCm, fill),
                    fill
            ));
        }
        return List.copyOf(readings);
    }

    private static double distance(double binHeightCm, double fillLevelPercent) {
        return round(binHeightCm * (100 - fillLevelPercent) / 100);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
