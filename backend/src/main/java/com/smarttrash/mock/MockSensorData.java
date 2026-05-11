package com.smarttrash.mock;

import com.smarttrash.model.SensorReading;
import com.smarttrash.model.SmartBinSensor;
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
                sensor(classifier, "bin-001", "Lixeira ESEG - Entrada Rua Vergueiro", "Paraiso", -23.5749, -46.6407, 120, List.of(18.0, 20.0, 25.0, 30.0)),
                sensor(classifier, "bin-002", "Lixeira ESEG - Biblioteca", "Paraiso", -23.5753, -46.6405, 120, List.of(35.0, 40.0, 50.0, 55.0)),
                sensor(classifier, "bin-003", "Lixeira Rua Vergueiro 1600", "Paraiso", -23.5759, -46.6403, 120, List.of(50.0, 60.0, 75.0, 85.0)),
                sensor(classifier, "bin-004", "Lixeira Rua Apeninos", "Paraiso", -23.5766, -46.6412, 120, List.of(40.0, 45.0, 55.0, 60.0)),
                sensor(classifier, "bin-005", "Lixeira Praca Rodrigues de Abreu", "Paraiso", -23.5772, -46.6401, 120, List.of(10.0, 15.0, 20.0, 25.0)),
                sensor(classifier, "bin-006", "Lixeira Sistema Etapa", "Vila Mariana", -23.5782, -46.6398, 120, List.of(60.0, 70.0, 80.0, 90.0)),
                sensor(classifier, "bin-007", "Lixeira Rua Vergueiro 1900", "Vila Mariana", -23.5788, -46.6396, 120, List.of(35.0, 40.0, 45.0, 50.0)),
                sensor(classifier, "bin-008", "Lixeira Colegio Etapa - Entrada", "Vila Mariana", -23.5793, -46.6394, 120, List.of(20.0, 25.0, 30.0, 35.0)),
                sensor(classifier, "bin-009", "Lixeira Colegio Etapa - Quadra", "Vila Mariana", -23.5797, -46.6391, 120, List.of(30.0, 35.0, 55.0, 65.0)),
                sensor(classifier, "bin-010", "Lixeira Rua Topazio", "Vila Mariana", -23.5803, -46.6388, 120, List.of(45.0, 50.0, 70.0, 80.0))
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
