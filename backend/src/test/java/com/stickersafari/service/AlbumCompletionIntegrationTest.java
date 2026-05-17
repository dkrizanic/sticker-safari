package com.stickersafari.service;

import com.stickersafari.dto.CollectorRequest;
import com.stickersafari.entity.Album;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.CompletedAlbum;
import com.stickersafari.entity.Sticker;
import com.stickersafari.repository.AlbumRepository;
import com.stickersafari.repository.CollectorRepository;
import com.stickersafari.repository.CompletedAlbumRepository;
import com.stickersafari.repository.StickerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Album Completion Integration Tests")
class AlbumCompletionIntegrationTest {

    @Autowired
    private AlbumService albumService;

    @Autowired
    private CollectorService collectorService;

    @Autowired
    private CollectorRepository collectorRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private StickerRepository stickerRepository;

    @Autowired
    private CompletedAlbumRepository completedAlbumRepository;

    private Album testAlbum;
    private Collector collector1;
    private Collector collector2;
    private Collector collector3;
    private Collector collector4;

    @BeforeEach
    void setUp() {
        // Create test album with stickers
        testAlbum = new Album();
        testAlbum.setName("Test Album");
        testAlbum = albumRepository.save(testAlbum);

        // Create 5 stickers for the album
        for (int i = 1; i <= 5; i++) {
            Sticker sticker = new Sticker();
            sticker.setName("Sticker " + i);
            sticker.setLatinName("Latin " + i);
            sticker.setDescription("Description " + i);
            sticker.setHabitat("Habitat " + i);
            sticker.setSrc("src" + i + ".jpg");
            sticker.setAlbum(testAlbum);
            stickerRepository.save(sticker);
        }
        testAlbum = albumRepository.findById(testAlbum.getId()).orElseThrow();

        // Create test collectors
        collector1 = createCollectorWithAllStickers("FirstCollector");
        collector2 = createCollectorWithAllStickers("SecondCollector");
        collector3 = createCollectorWithAllStickers("ThirdCollector");
        collector4 = createCollectorWithAllStickers("FourthCollector");
    }

    private Collector createCollectorWithAllStickers(String username) {
        // Create collector using service (this handles User creation)
        CollectorRequest request = new CollectorRequest();
        request.setUsername(username);
        Collector collector = collectorService.createCollector(request);
        
        // Add all stickers from the album to this collector
        for (Sticker sticker : testAlbum.getStickers()) {
            try {
                collector.addSticker(sticker.getId());
            } catch (Exception e) {
                // Sticker already added
            }
        }
        
        return collectorRepository.save(collector);
    }

    @Test
    @DisplayName("First collector to complete album should receive a star")
    void testFirstCollectorGetsStar() throws Exception {
        // When
        albumService.completeAlbum(testAlbum.getId(), collector1.getId());

        // Then
        Collector updated = collectorRepository.findById(collector1.getId()).orElseThrow();
        assertThat(updated.getStars()).isEqualTo(1);
        
        List<CompletedAlbum> completions = completedAlbumRepository.findAll();
        assertThat(completions).hasSize(1);
        assertThat(completions.get(0).getCollector().getId()).isEqualTo(collector1.getId());
    }

    @Test
    @DisplayName("Second and third collectors should receive stars")
    void testSecondAndThirdCollectorsGetStars() throws Exception {
        // Given - First collector completes
        albumService.completeAlbum(testAlbum.getId(), collector1.getId());

        // When - Second and third complete
        albumService.completeAlbum(testAlbum.getId(), collector2.getId());
        albumService.completeAlbum(testAlbum.getId(), collector3.getId());

        // Then
        Collector updated2 = collectorRepository.findById(collector2.getId()).orElseThrow();
        Collector updated3 = collectorRepository.findById(collector3.getId()).orElseThrow();
        
        assertThat(updated2.getStars()).isEqualTo(1);
        assertThat(updated3.getStars()).isEqualTo(1);
    }

    @Test
    @DisplayName("Fourth collector should NOT receive a star")
    void testFourthCollectorDoesNotGetStar() throws Exception {
        // Given - First three collectors complete
        albumService.completeAlbum(testAlbum.getId(), collector1.getId());
        albumService.completeAlbum(testAlbum.getId(), collector2.getId());
        albumService.completeAlbum(testAlbum.getId(), collector3.getId());

        // When - Fourth collector completes
        albumService.completeAlbum(testAlbum.getId(), collector4.getId());

        // Then - Should have 0 stars
        Collector updated4 = collectorRepository.findById(collector4.getId()).orElseThrow();
        assertThat(updated4.getStars()).isEqualTo(0);
        
        // But completion should still be recorded
        List<CompletedAlbum> completions = completedAlbumRepository.findAll();
        assertThat(completions).hasSize(4);
    }

    @Test
    @DisplayName("Stickers should be removed from collector after completion")
    void testStickersRemovedAfterCompletion() throws Exception {
        // Given
        int originalStickerCount = collector1.getOwnedStickers().size();
        assertThat(originalStickerCount).isEqualTo(5);

        // When
        albumService.completeAlbum(testAlbum.getId(), collector1.getId());

        // Then
        Collector updated = collectorRepository.findById(collector1.getId()).orElseThrow();
        assertThat(updated.getOwnedStickers()).isEmpty();
    }

    @Test
    @DisplayName("Collector can complete same album multiple times")
    void testMultipleCompletionsFromSameCollector() throws Exception {
        // Given - First completion
        albumService.completeAlbum(testAlbum.getId(), collector1.getId());
        Collector afterFirst = collectorRepository.findById(collector1.getId()).orElseThrow();
        assertThat(afterFirst.getStars()).isEqualTo(1);

        // When - Add stickers again and complete
        Map<Long, Integer> newStickers = new HashMap<>();
        for (Sticker sticker : testAlbum.getStickers()) {
            newStickers.put(sticker.getId(), 1);
        }
        afterFirst.setOwnedStickers(newStickers);
        collectorRepository.save(afterFirst);

        albumService.completeAlbum(testAlbum.getId(), collector1.getId());

        // Then - Should have same number of stars (already got star for first completion)
        Collector afterSecond = collectorRepository.findById(collector1.getId()).orElseThrow();
        assertThat(afterSecond.getStars()).isEqualTo(1); // Still 1 star

        // But should have 2 completion records
        List<CompletedAlbum> completions = completedAlbumRepository.findAll();
        assertThat(completions.stream()
                .filter(c -> c.getCollector().getId().equals(collector1.getId()))
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should throw exception when collector missing stickers")
    void testCompletionFailsWhenMissingStickers() {
        // Given - Collector with only some stickers
        Collector incomplete = new Collector();
        incomplete.setUsername("IncompleteCollector");
        incomplete.setStars(0);
        
        Map<Long, Integer> partialStickers = new HashMap<>();
        List<Sticker> stickers = testAlbum.getStickers();
        partialStickers.put(stickers.get(0).getId(), 1); // Only first sticker
        incomplete.setOwnedStickers(partialStickers);
        incomplete = collectorRepository.save(incomplete);

        // When & Then
        final Long incompleteId = incomplete.getId();
        assertThatThrownBy(() -> albumService.completeAlbum(testAlbum.getId(), incompleteId))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should throw exception when album does not exist")
    void testCompletionFailsWhenAlbumNotFound() {
        // When & Then
        assertThatThrownBy(() -> albumService.completeAlbum(999L, collector1.getId()))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should throw exception when collector does not exist")
    void testCompletionFailsWhenCollectorNotFound() {
        // When & Then
        assertThatThrownBy(() -> albumService.completeAlbum(testAlbum.getId(), 999L))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Multiple collectors completing different albums should track correctly")
    void testMultipleAlbumsAndCollectors() throws Exception {
        // Given - Create second album
        Album album2 = new Album();
        album2.setName("Second Album");
        album2 = albumRepository.save(album2);

        Sticker sticker2 = new Sticker();
        sticker2.setName("Album2 Sticker");
        sticker2.setLatinName("Latin");
        sticker2.setDescription("Desc");
        sticker2.setHabitat("Habitat");
        sticker2.setSrc("src.jpg");
        sticker2.setAlbum(album2);
        stickerRepository.save(sticker2);

        // Give collector1 stickers for album2
        album2 = albumRepository.findById(album2.getId()).orElseThrow();
        Map<Long, Integer> album2Stickers = new HashMap<>();
        for (Sticker s : album2.getStickers()) {
            album2Stickers.put(s.getId(), 1);
        }
        
        Collector c1 = collectorRepository.findById(collector1.getId()).orElseThrow();
        c1.getOwnedStickers().putAll(album2Stickers);
        collectorRepository.save(c1);

        // When - Complete first album with collector1, second album with collector1
        albumService.completeAlbum(testAlbum.getId(), collector1.getId());
        albumService.completeAlbum(album2.getId(), collector1.getId());

        // Then - Should have 2 stars
        Collector updated = collectorRepository.findById(collector1.getId()).orElseThrow();
        assertThat(updated.getStars()).isEqualTo(2);
        
        List<CompletedAlbum> completions = completedAlbumRepository.findAll();
        assertThat(completions).hasSize(2);
    }
}
