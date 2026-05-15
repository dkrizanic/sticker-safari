package com.stickersafari.service;

import com.stickersafari.dto.CollectorRequest;
import com.stickersafari.entity.Collector;
import com.stickersafari.exception.EntityNotFoundException;
import com.stickersafari.repository.CollectorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Collector CRUD Operations Integration Tests")
class CollectorCrudIntegrationTest {

    @Autowired
    private CollectorService collectorService;

    @Autowired
    private CollectorRepository collectorRepository;

    @Test
    @DisplayName("Should create new collector successfully")
    void testCreateCollector() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("NewUser");

        // When
        Collector response = collectorService.createCollector(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("NewUser");
        assertThat(response.getId()).isNotNull();
        assertThat(response.getStars()).isEqualTo(0);
        assertThat(response.getDailyStickerCount()).isEqualTo(0);

        Collector saved = collectorRepository.findById(response.getId()).orElseThrow();
        assertThat(saved.getUsername()).isEqualTo("NewUser");
    }

    @Test
    @DisplayName("Should retrieve all collectors")
    void testGetAllCollectors() {
        // Given - Collectors exist from seed data (Bob, Jack, Anne, Ema)

        // When
        List<Collector> collectors = collectorService.getAllCollectors();

        // Then
        assertThat(collectors).isNotEmpty();
        assertThat(collectors)
                .extracting(Collector::getUsername)
                .contains("Bob", "Jack", "Anne", "Ema");
    }

    @Test
    @DisplayName("Should find collector by username")
    void testFindByUsername() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("UniqueUser");
        collectorService.createCollector(request);

        // When
        Collector found = collectorService.findByUsername("UniqueUser");

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("UniqueUser");
    }

    @Test
    @DisplayName("Should update collector username")
    void testUpdateCollector() {
        // Given
        CollectorRequest createRequest = new CollectorRequest();
        createRequest.setUsername("OldUsername");
        Collector created = collectorService.createCollector(createRequest);

        CollectorRequest updateRequest = new CollectorRequest();
        updateRequest.setUsername("NewUsername");

        // When
        Collector updated = collectorService.updateCollector(created.getId(), updateRequest);

        // Then
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getUsername()).isEqualTo("NewUsername");

        Collector saved = collectorRepository.findById(created.getId()).orElseThrow();
        assertThat(saved.getUsername()).isEqualTo("NewUsername");
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent collector")
    void testUpdateNonExistentCollector() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("NewName");

        // When/Then
        assertThatThrownBy(() -> collectorService.updateCollector(99999L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Collector not found");
    }

    @Test
    @DisplayName("Should delete collector successfully")
    void testDeleteCollector() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("UserToDelete");
        Collector created = collectorService.createCollector(request);

        // When
        collectorService.deleteCollector(created.getId());

        // Then
        assertThat(collectorRepository.findById(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent collector")
    void testDeleteNonExistentCollector() {
        // When/Then
        assertThatThrownBy(() -> collectorService.deleteCollector(99999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Collector not found");
    }

    @Test
    @DisplayName("Should get collector by id")
    void testGetCollectorById() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("TestUser");
        Collector created = collectorService.createCollector(request);

        // When
        Collector found = collectorService.findById(created.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getUsername()).isEqualTo("TestUser");
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent collector")
    void testGetNonExistentCollector() {
        // When/Then
        assertThatThrownBy(() -> collectorService.findById(99999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Collector not found");
    }

    @Test
    @DisplayName("Should initialize new collector with default values")
    void testCollectorDefaultValues() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("DefaultUser");

        // When
        Collector response = collectorService.createCollector(request);

        // Then
        assertThat(response.getStars()).isEqualTo(0);
        assertThat(response.getDailyStickerCount()).isEqualTo(0);
        assertThat(response.getLastPackOpenDate()).isNull();
        assertThat(response.getOwnedStickers()).isEmpty();
    }
}
