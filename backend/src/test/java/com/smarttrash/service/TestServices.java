package com.smarttrash.service;

import com.smarttrash.repository.InMemorySensorRepository;

final class TestServices {

    private TestServices() {
    }

    static SensorService sensorService() {
        var classifier = new BinStatusClassifier();
        return new SensorService(new InMemorySensorRepository(classifier), classifier);
    }

    static DashboardService dashboardService() {
        return new DashboardService(sensorService());
    }
}
