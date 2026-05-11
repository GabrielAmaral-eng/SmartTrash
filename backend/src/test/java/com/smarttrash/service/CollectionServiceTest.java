package com.smarttrash.service;

import com.smarttrash.testsupport.TestCollectionRepository;
import com.smarttrash.testsupport.TestSensorRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionServiceTest {

    private final BinStatusClassifier classifier = new BinStatusClassifier();
    private final TestSensorRepository sensorRepository = new TestSensorRepository(classifier);
    private final CollectionService service = new CollectionService(new TestCollectionRepository(sensorRepository), sensorRepository);

    @Test
    void listsMockedCollectionAssignments() {
        var collections = service.listCollections();

        assertThat(collections.collections()).isNotEmpty();
        assertThat(collections.collections()).allSatisfy(collection -> {
            assertThat(collection.sensorId()).isNotBlank();
            assertThat(collection.responsibleTeam()).isNotBlank();
            assertThat(collection.progressPercent()).isBetween(0, 100);
        });
    }

    @Test
    void allocatesTeamForSensorsAboveSeventyPercent() {
        var assignment = service.allocateTeam("bin-003");

        assertThat(assignment.sensorId()).isEqualTo("bin-003");
        assertThat(assignment.fillLevelPercent()).isGreaterThan(70);
        assertThat(assignment.responsibleTeam()).isEqualTo("Equipe Operacional");
        assertThat(service.listCollections().collections())
                .anySatisfy(collection -> assertThat(collection.sensorId()).isEqualTo("bin-003"));
    }

    @Test
    void doesNotAllocateTeamForSensorsAtOrBelowSeventyPercent() {
        assertThatThrownBy(() -> service.allocateTeam("bin-009"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mais de 70%");
    }

    @Test
    void buildsScheduledRouteOnlyForSensorsAboveFiftyPercent() {
        var route = service.scheduledRoute();

        assertThat(route.thresholdPercent()).isEqualTo(50);
        assertThat(route.stops()).isNotEmpty();
        assertThat(route.stops()).allSatisfy(stop -> assertThat(stop.fillLevelPercent()).isGreaterThan(50));
    }
}
