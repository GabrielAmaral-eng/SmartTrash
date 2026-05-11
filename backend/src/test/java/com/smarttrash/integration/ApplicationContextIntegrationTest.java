package com.smarttrash.integration;

import com.smarttrash.repository.CollectionRepository;
import com.smarttrash.repository.ProfileRepository;
import com.smarttrash.repository.SensorRepository;
import com.smarttrash.service.BinStatusClassifier;
import com.smarttrash.testsupport.TestCollectionRepository;
import com.smarttrash.testsupport.TestProfileRepository;
import com.smarttrash.testsupport.TestSensorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "smarttrash.data-source=test")
@Import(ApplicationContextIntegrationTest.TestConfig.class)
class ApplicationContextIntegrationTest {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        SensorRepository sensorRepository(BinStatusClassifier classifier) {
            return new TestSensorRepository(classifier);
        }

        @Bean
        CollectionRepository collectionRepository(SensorRepository sensorRepository) {
            return new TestCollectionRepository(sensorRepository);
        }

        @Bean
        ProfileRepository profileRepository() {
            return new TestProfileRepository();
        }
    }
}
