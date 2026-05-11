package com.smarttrash.controller;

import com.smarttrash.testsupport.TestSensorRepository;
import com.smarttrash.testsupport.TestProfileRepository;
import com.smarttrash.service.BinStatusClassifier;
import com.smarttrash.service.AuthService;
import com.smarttrash.service.DashboardService;
import com.smarttrash.service.SensorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@Import({DashboardController.class, DashboardControllerTest.TestConfig.class})
class DashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void exposesDashboardSummary() throws Exception {
        mockMvc.perform(get("/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSensors").isNumber())
                .andExpect(jsonPath("$.byStatus.EMPTY").isNumber());
    }

    @Test
    void exposesDashboardHistory() throws Exception {
        mockMvc.perform(get("/dashboard/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").isArray())
                .andExpect(jsonPath("$.points[0].averageFillLevelPercent").isNumber());
    }

    @Test
    void exposesDashboardRegions() throws Exception {
        mockMvc.perform(get("/dashboard/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regions").isArray())
                .andExpect(jsonPath("$.regions[0].region").isString());
    }

    @Configuration
    static class TestConfig {
        @Bean
        BinStatusClassifier classifier() {
            return new BinStatusClassifier();
        }

        @Bean
        TestSensorRepository repository(BinStatusClassifier classifier) {
            return new TestSensorRepository(classifier);
        }

        @Bean
        SensorService sensorService(TestSensorRepository repository, BinStatusClassifier classifier) {
            return new SensorService(repository, classifier);
        }

        @Bean
        DashboardService dashboardService(SensorService sensorService) {
            return new DashboardService(sensorService);
        }

        @Bean
        TestProfileRepository profileRepository() {
            return new TestProfileRepository();
        }

        @Bean
        AuthService authService(TestProfileRepository profileRepository) {
            return new AuthService(profileRepository);
        }
    }
}
