package com.stickersafari.service;

import com.stickersafari.dto.CollectorRequest;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.exception.DailyLimitExceededException;
import com.stickersafari.repository.CollectorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Daily Pack Opening Limit Integration Tests")
class DailyLimitIntegrationTest {

    @Autowired
    private StickerService stickerService;

    @Autowired
    private CollectorService collectorService;

    @Autowired
    private CollectorRepository collectorRepository;

    @Test
    @DisplayName("Should allow opening pack when within daily limit")
    void testOpenPackWithinLimit() {
        // Given
        CollectorRequest collectorRequest = new CollectorRequest();
        collectorRequest.setUsername("TestCollector");
        Collector collector = collectorService.createCollector(collectorRequest);

        // When - Open first pack (5 stickers)
        List<Sticker> stickers = stickerService.openPack(1L, collector.getId(), 5);

        // Then
        assertThat(stickers).hasSize(5);
        
        Collector updated = collectorRepository.findById(collector.getId()).orElseThrow();
        assertThat(updated.getDailyStickerCount()).isEqualTo(5);
        assertThat(updated.getLastPackOpenDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Should allow opening multiple packs within daily limit")
    void testOpenMultiplePacksWithinLimit() {
        // Given
        CollectorRequest collectorRequest = new CollectorRequest();
        collectorRequest.setUsername("MultiPackCollector");
        Collector collector = collectorService.createCollector(collectorRequest);

        // When - Open first pack
        stickerService.openPack(1L, collector.getId(), 5);
        
        // Then - Can open second pack
        List<Sticker> secondPack = stickerService.openPack(1L, collector.getId(), 5);
        assertThat(secondPack).hasSize(5);

        Collector updated = collectorRepository.findById(collector.getId()).orElseThrow();
        assertThat(updated.getDailyStickerCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should throw exception when exceeding daily limit of 20 stickers")
    void testExceedDailyLimit() {
        // Given
        CollectorRequest collectorRequest = new CollectorRequest();
        collectorRequest.setUsername("LimitExceeder");
        Collector collector = collectorService.createCollector(collectorRequest);

        // When - Open packs up to 15 stickers
        stickerService.openPack(1L, collector.getId(), 5);
        stickerService.openPack(1L, collector.getId(), 5);
        stickerService.openPack(1L, collector.getId(), 5);

        // Then - Opening another pack of 6 should fail (15 + 6 = 21 > 20)
        assertThatThrownBy(() -> stickerService.openPack(1L, collector.getId(), 6))
                .isInstanceOf(DailyLimitExceededException.class)
                .hasMessageContaining("Daily limit of 20 stickers reached");

        Collector updated = collectorRepository.findById(collector.getId()).orElseThrow();
        assertThat(updated.getDailyStickerCount()).isEqualTo(15);
    }

    @Test
    @DisplayName("Should reset daily counter on new day")
    void testDailyCounterReset() {
        // Given
        CollectorRequest collectorRequest = new CollectorRequest();
        collectorRequest.setUsername("ResetTester");
        Collector collector = collectorService.createCollector(collectorRequest);

        // Simulate opening packs yesterday
        Collector entity = collectorRepository.findById(collector.getId()).orElseThrow();
        entity.setLastPackOpenDate(LocalDate.now().minusDays(1));
        entity.setDailyStickerCount(15);
        collectorRepository.save(entity);

        // When - Open pack today
        List<Sticker> stickers = stickerService.openPack(1L, collector.getId(), 5);

        // Then - Counter should be reset
        assertThat(stickers).hasSize(5);
        
        Collector updated = collectorRepository.findById(collector.getId()).orElseThrow();
        assertThat(updated.getDailyStickerCount()).isEqualTo(5);
        assertThat(updated.getLastPackOpenDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Should allow exactly 20 stickers per day")
    void testExactlyTwentyStickers() {
        // Given
        CollectorRequest collectorRequest = new CollectorRequest();
        collectorRequest.setUsername("ExactLimitTester");
        Collector collector = collectorService.createCollector(collectorRequest);

        // When - Open packs totaling exactly 20 stickers
        stickerService.openPack(1L, collector.getId(), 5);
        stickerService.openPack(1L, collector.getId(), 5);
        stickerService.openPack(1L, collector.getId(), 5);
        List<Sticker> lastPack = stickerService.openPack(1L, collector.getId(), 5);

        // Then - Should succeed
        assertThat(lastPack).hasSize(5);
        
        Collector updated = collectorRepository.findById(collector.getId()).orElseThrow();
        assertThat(updated.getDailyStickerCount()).isEqualTo(20);
    }

    @Test
    @DisplayName("Should reject any pack opening after reaching exactly 20 stickers")
    void testRejectAfterReachingLimit() {
        // Given
        CollectorRequest collectorRequest = new CollectorRequest();
        collectorRequest.setUsername("PostLimitTester");
        Collector collector = collectorService.createCollector(collectorRequest);

        // Open packs totaling 20 stickers
        stickerService.openPack(1L, collector.getId(), 5);
        stickerService.openPack(1L, collector.getId(), 5);
        stickerService.openPack(1L, collector.getId(), 5);
        stickerService.openPack(1L, collector.getId(), 5);

        // When/Then - Any additional pack should fail
        assertThatThrownBy(() -> stickerService.openPack(1L, collector.getId(), 1))
                .isInstanceOf(DailyLimitExceededException.class);
    }

    @Test
    @DisplayName("Should handle first pack opening correctly")
    void testFirstPackOpening() {
        // Given
        CollectorRequest collectorRequest = new CollectorRequest();
        collectorRequest.setUsername("FirstTimer");
        Collector collector = collectorService.createCollector(collectorRequest);

        Collector entity = collectorRepository.findById(collector.getId()).orElseThrow();
        assertThat(entity.getLastPackOpenDate()).isNull();
        assertThat(entity.getDailyStickerCount()).isEqualTo(0);

        // When
        List<Sticker> stickers = stickerService.openPack(1L, collector.getId(), 5);

        // Then
        assertThat(stickers).hasSize(5);
        
        Collector updated = collectorRepository.findById(collector.getId()).orElseThrow();
        assertThat(updated.getDailyStickerCount()).isEqualTo(5);
        assertThat(updated.getLastPackOpenDate()).isEqualTo(LocalDate.now());
    }
}
