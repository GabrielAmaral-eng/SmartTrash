package com.smarttrash.service;

import com.smarttrash.repository.InMemoryCollectionRepository;
import com.smarttrash.repository.InMemorySensorRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionServiceTest {

    private final BinStatusClassifier classifier = new BinStatusClassifier();
    private final InMemorySensorRepository sensorRepository = new InMemorySensorRepository(classifier);
    private final CollectionService service = new CollectionService(new InMemoryCollectionRepository(sensorRepository), sensorRepository);

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
        assertThat(assignment.responsibleTeam()).contains("Sul");
        assertThat(service.listCollections().collections())
                .anySatisfy(collection -> assertThat(collection.sensorId()).isEqualTo("bin-003"));
    }

    @Test
    void doesNotAllocateTeamForSensorsAtOrBelowSeventyPercent() {
        assertThatThrownBy(() -> service.allocateTeam("bin-009"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mais de 70%");
    }
}
