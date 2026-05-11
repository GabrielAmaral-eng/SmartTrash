package com.smarttrash.service;

import com.smarttrash.testsupport.TestSensorRepository;

final class TestServices {

    private TestServices() {
    }

    static SensorService sensorService() {
        var classifier = new BinStatusClassifier();
        return new SensorService(new TestSensorRepository(classifier), classifier);
    }

    static DashboardService dashboardService() {
        return new DashboardService(sensorService());
    }
}
