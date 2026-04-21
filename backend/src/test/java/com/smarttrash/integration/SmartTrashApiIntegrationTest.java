package com.smarttrash.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
<<<<<<< HEAD
import org.springframework.http.HttpMethod;
=======
>>>>>>> 8b2dcbe42d67f585adb5fa766588e67e21470521

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SmartTrashApiIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void dashboardConsultationFlowWorks() {
        var summary = restTemplate.getForEntity(url("/dashboard/summary"), String.class);
        var history = restTemplate.getForEntity(url("/dashboard/history"), String.class);
        var regions = restTemplate.getForEntity(url("/dashboard/regions"), String.class);

        assertThat(summary.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(history.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(regions.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(summary.getBody()).contains("totalSensors");
        assertThat(history.getBody()).contains("averageFillLevelPercent");
        assertThat(regions.getBody()).contains("regions");
    }

    @Test
    void sensorsConsultationFlowWorks() {
        var sensors = restTemplate.getForEntity(url("/sensors"), String.class);
        var detail = restTemplate.getForEntity(url("/sensors/bin-001"), String.class);
        var locations = restTemplate.getForEntity(url("/sensors/locations"), String.class);

        assertThat(sensors.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(detail.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(locations.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(sensors.getBody()).contains("bin-001");
        assertThat(detail.getBody()).contains("Centro");
        assertThat(locations.getBody()).contains("latitude");
    }

    @Test
    void sensorHistoryFlowWorks() {
        var history = restTemplate.getForEntity(url("/sensors/bin-001/history"), String.class);

        assertThat(history.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(history.getBody()).contains("sensorId");
        assertThat(history.getBody()).contains("fillLevelPercent");
    }

<<<<<<< HEAD
    @Test
    void collectionAllocationFlowWorks() {
        var allocation = restTemplate.exchange(url("/collections/allocations/bin-003"), HttpMethod.POST, null, String.class);
        var collections = restTemplate.getForEntity(url("/collections"), String.class);

        assertThat(allocation.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(collections.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(allocation.getBody()).contains("bin-003");
        assertThat(collections.getBody()).contains("responsibleTeam");
        assertThat(collections.getBody()).contains("bin-003");
    }

=======
>>>>>>> 8b2dcbe42d67f585adb5fa766588e67e21470521
    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
