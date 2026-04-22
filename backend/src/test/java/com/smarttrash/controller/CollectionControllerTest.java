package com.smarttrash.controller;

import com.smarttrash.config.ApiExceptionHandler;
import com.smarttrash.repository.InMemoryCollectionRepository;
import com.smarttrash.repository.InMemorySensorRepository;
import com.smarttrash.service.BinStatusClassifier;
import com.smarttrash.service.CollectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CollectionController.class)
@Import({CollectionController.class, CollectionControllerTest.TestConfig.class, ApiExceptionHandler.class})
class CollectionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void exposesCollectionList() throws Exception {
        mockMvc.perform(get("/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collections").isArray())
                .andExpect(jsonPath("$.collections[0].responsibleTeam").isString());
    }

    @Test
    void allocatesTeamForEligibleSensor() throws Exception {
        mockMvc.perform(post("/collections/allocations/bin-003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorId").value("bin-003"))
                .andExpect(jsonPath("$.estimatedCollectionTime").isString());
    }

    @Test
    void rejectsIneligibleSensorAllocation() throws Exception {
        mockMvc.perform(post("/collections/allocations/bin-001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isString());
    }

    @Configuration
    static class TestConfig {
        @Bean
        BinStatusClassifier classifier() {
            return new BinStatusClassifier();
        }

        @Bean
        InMemorySensorRepository sensorRepository(BinStatusClassifier classifier) {
            return new InMemorySensorRepository(classifier);
        }

        @Bean
        InMemoryCollectionRepository collectionRepository(InMemorySensorRepository sensorRepository) {
            return new InMemoryCollectionRepository(sensorRepository);
        }

        @Bean
        CollectionService collectionService(InMemoryCollectionRepository collectionRepository, InMemorySensorRepository sensorRepository) {
            return new CollectionService(collectionRepository, sensorRepository);
        }
    }
}
