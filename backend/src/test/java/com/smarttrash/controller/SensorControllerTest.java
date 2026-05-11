package com.smarttrash.controller;

import com.smarttrash.config.ApiExceptionHandler;
import com.smarttrash.repository.InMemoryProfileRepository;
import com.smarttrash.repository.InMemorySensorRepository;
import com.smarttrash.service.AuthService;
import com.smarttrash.service.BinStatusClassifier;
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

@WebMvcTest(SensorController.class)
@Import({SensorController.class, SensorControllerTest.TestConfig.class, ApiExceptionHandler.class})
class SensorControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void exposesSensorList() throws Exception {
        mockMvc.perform(get("/sensors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensors").isArray())
                .andExpect(jsonPath("$.sensors[0].status").isString());
    }

    @Test
    void exposesSensorDetail() throws Exception {
        mockMvc.perform(get("/sensors/bin-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("bin-001"))
                .andExpect(jsonPath("$.history").isArray());
    }

    @Test
    void exposesSensorHistory() throws Exception {
        mockMvc.perform(get("/sensors/bin-001/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorId").value("bin-001"))
                .andExpect(jsonPath("$.points").isArray());
    }

    @Test
    void exposesSensorLocations() throws Exception {
        mockMvc.perform(get("/sensors/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locations").isArray())
                .andExpect(jsonPath("$.locations[0].latitude").isNumber());
    }

    @Test
    void returns404ForMissingSensor() throws Exception {
        mockMvc.perform(get("/sensors/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isString());
    }

    @Configuration
    static class TestConfig {
        @Bean
        BinStatusClassifier classifier() {
            return new BinStatusClassifier();
        }

        @Bean
        InMemorySensorRepository repository(BinStatusClassifier classifier) {
            return new InMemorySensorRepository(classifier);
        }

        @Bean
        SensorService sensorService(InMemorySensorRepository repository, BinStatusClassifier classifier) {
            return new SensorService(repository, classifier);
        }

        @Bean
        InMemoryProfileRepository profileRepository() {
            return new InMemoryProfileRepository();
        }

        @Bean
        AuthService authService(InMemoryProfileRepository profileRepository) {
            return new AuthService(profileRepository);
        }
    }
}
