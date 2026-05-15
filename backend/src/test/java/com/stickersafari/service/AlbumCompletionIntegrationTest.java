package com.stickersafari.service;

import com.stickersafari.dto.AlbumRequest;
import com.stickersafari.dto.CollectorRequest;
import com.stickersafari.dto.StickerRequest;
import com.stickersafari.entity.Album;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.entity.CompletedAlbum;
import com.stickersafari.entity.Notification;
import com.stickersafari.repository.CollectorRepository;
import com.stickersafari.repository.CompletedAlbumRepository;
import com.stickersafari.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.util.List;

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
    private StickerService stickerService;

    @Autowired
    private CollectorService collectorService;

    @Autowired
    private CollectorRepository collectorRepository;

    @Autowired
    private CompletedAlbumRepository completedAlbumRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Should detect when collector can complete an album")
    void testCanCompleteAlbum() {
        // Given - Create small album with 2 stickers
        AlbumRequest albumRequest = new AlbumRequest();
        albumRequest.setName("Small Album");
        Album album = albumService.createAlbum(albumRequest);
        entityManager.flush();

        StickerRequest sticker1 = new StickerRequest();
        sticker1.setName("Sticker 1");
        sticker1.setLatinName("Latin 1");
        sticker1.setHabitat("Habitat 1");
        sticker1.setDescription("Desc 1");
        sticker1.setSrc("https://example.com/1.jpg");
        sticker1.setAlbumId(album.getId());
        Sticker s1 = stickerService.createSticker(sticker1);

        StickerRequest sticker2 = new StickerRequest();
        sticker2.setName("Sticker 2");
        sticker2.setLatinName("Latin 2");
        sticker2.setHabitat("Habitat 2");
        sticker2.setDescription("Desc 2");
        sticker2.setSrc("https://example.com/2.jpg");
        sticker2.setAlbumId(album.getId());
        Sticker s2 = stickerService.createSticker(sticker2);
        entityManager.flush();

        CollectorRequest collectorRequest = new CollectorRequest();
        collectorRequest.setUsername("CompletionTester");
        Collector collector = collectorService.createCollector(collectorRequest);

        // Give collector both stickers
        Collector entity = collectorRepository.findById(collector.getId()).orElseThrow();
        entity.addSticker(s1.getId());
        entity.addSticker(s2.getId());
        collectorRepository.save(entity);

        // When
        boolean canComplete = albumService.canCompleteAlbum(album.getId(), collector.getId());

        // Then
        assertThat(canComplete).isTrue();
    }

    @Test
    @DisplayName("Should return false when collector cannot complete album")
    void testCannotCompleteAlbum() {
        // Given - Create album with 2 stickers
        AlbumRequest albumRequest = new AlbumRequest();
        albumRequest.setName("Incomplete Album");
        Album album = albumService.createAlbum(albumRequest);
        entityManager.flush();

        StickerRequest sticker1 = new StickerRequest();
        sticker1.setName("Sticker 1");
        sticker1.setLatinName("Latin 1");
        sticker1.setHabitat("Habitat 1");
        sticker1.setDescription("Desc 1");
        sticker1.setSrc("https://example.com/1.jpg");
        sticker1.setAlbumId(album.getId());
        Sticker s1 = stickerService.createSticker(sticker1);

        StickerRequest sticker2 = new StickerRequest();
        sticker2.setName("Sticker 2");
        sticker2.setLatinName("Latin 2");
        sticker2.setHabitat("Habitat 2");
        sticker2.setDescription("Desc 2");
        sticker2.setSrc("https://example.com/2.jpg");
        sticker2.setAlbumId(album.getId());
        stickerService.createSticker(sticker2);
        entityManager.flush();

        CollectorRequest collectorRequest = new CollectorRequest();
        collectorRequest.setUsername("IncompleteTester");
        Collector collector = collectorService.createCollector(collectorRequest);

        // Give collector only one sticker
        Collector entity = collectorRepository.findById(collector.getId()).orElseThrow();
        entity.addSticker(s1.getId());
        collectorRepository.save(entity);

        // When
        boolean canComplete = albumService.canCompleteAlbum(album.getId(), collector.getId());

        // Then
        assertThat(canComplete).isFalse();
    }

    @Test
    @DisplayName("Should complete album and award star")
    void testCompleteAlbum() {
        // Given - Create small album with 2 stickers
        AlbumRequest albumRequest = new AlbumRequest();
        albumRequest.setName("Completable Album");
        Album album = albumService.createAlbum(albumRequest);
        entityManager.flush();

        StickerRequest sticker1 = new StickerRequest();
        sticker1.setName("Sticker 1");
        sticker1.setLatinName("Latin 1");
        sticker1.setHabitat("Habitat 1");
        sticker1.setDescription("Desc 1");
        sticker1.setSrc("https://example.com/1.jpg");
        sticker1.setAlbumId(album.getId());
        Sticker s1 = stickerService.createSticker(sticker1);

        StickerRequest sticker2 = new StickerRequest();
        sticker2.setName("Sticker 2");
        sticker2.setLatinName("Latin 2");
        sticker2.setHabitat("Habitat 2");
        sticker2.setDescription("Desc 2");
        sticker2.setSrc("https://example.com/2.jpg");
        sticker2.setAlbumId(album.getId());
        Sticker s2 = stickerService.createSticker(sticker2);
        entityManager.flush();

        CollectorRequest collectorRequest = new CollectorRequest();
        collectorRequest.setUsername("StarEarner");
        Collector collector = collectorService.createCollector(collectorRequest);

        // Give collector both stickers (with extras for duplicates)
        Collector entity = collectorRepository.findById(collector.getId()).orElseThrow();
        entity.addSticker(s1.getId());
        entity.addSticker(s1.getId()); // duplicate
        entity.addSticker(s2.getId());
        collectorRepository.save(entity);

        int initialStars = entity.getStars();

        // When
        albumService.completeAlbum(album.getId(), collector.getId());

        // Then
        Collector updated = collectorRepository.findById(collector.getId()).orElseThrow();
        assertThat(updated.getStars()).isEqualTo(initialStars + 1);

        // Stickers should be removed (one of each)
        assertThat(updated.getOwnedStickers().get(s1.getId())).isEqualTo(1); // had 2, lost 1
        assertThat(updated.getOwnedStickers().containsKey(s2.getId())).isFalse(); // had 1, lost 1

        // CompletedAlbum record should exist
        List<CompletedAlbum> completions = completedAlbumRepository.findAll();
        assertThat(completions).isNotEmpty();
        assertThat(completions)
                .anyMatch(ca -> ca.getCollector().getId().equals(collector.getId()) 
                        && ca.getAlbum().getId().equals(album.getId()));

        // Notification should be created
        List<Notification> notifications = notificationRepository.findByRecipientId(collector.getId());
        assertThat(notifications)
                .anyMatch(n -> n.getMessage().contains("completed") && n.getMessage().contains(album.getName()));
    }

    @Test
    @DisplayName("Should throw exception when trying to complete album without all stickers")
    void testCompleteAlbumWithoutAllStickers() {
        // Given - Create album with 2 stickers
        AlbumRequest albumRequest = new AlbumRequest();
        albumRequest.setName("Incomplete Test Album");
        Album album = albumService.createAlbum(albumRequest);
        entityManager.flush();

        StickerRequest sticker1 = new StickerRequest();
        sticker1.setName("Sticker 1");
        sticker1.setLatinName("Latin 1");
        sticker1.setHabitat("Habitat 1");
        sticker1.setDescription("Desc 1");
        sticker1.setSrc("https://example.com/1.jpg");
        sticker1.setAlbumId(album.getId());
        Sticker s1 = stickerService.createSticker(sticker1);

        StickerRequest sticker2 = new StickerRequest();
        sticker2.setName("Sticker 2");
        sticker2.setLatinName("Latin 2");
        sticker2.setHabitat("Habitat 2");
        sticker2.setDescription("Desc 2");
        sticker2.setSrc("https://example.com/2.jpg");
        sticker2.setAlbumId(album.getId());
        stickerService.createSticker(sticker2);
        entityManager.flush();

        CollectorRequest collectorRequest = new CollectorRequest();
        collectorRequest.setUsername("IncompleteUser");
        Collector collector = collectorService.createCollector(collectorRequest);

        // Give collector only one sticker
        Collector entity = collectorRepository.findById(collector.getId()).orElseThrow();
        entity.addSticker(s1.getId());
        collectorRepository.save(entity);

        // When/Then
        assertThatThrownBy(() -> albumService.completeAlbum(album.getId(), collector.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot complete album");
    }

    @Test
    @DisplayName("Should allow completing same album multiple times")
    void testCompleteAlbumMultipleTimes() {
        // Given - Create small album
        AlbumRequest albumRequest = new AlbumRequest();
        albumRequest.setName("Repeatable Album");
        Album album = albumService.createAlbum(albumRequest);
        entityManager.flush();

        StickerRequest sticker1 = new StickerRequest();
        sticker1.setName("Sticker 1");
        sticker1.setLatinName("Latin 1");
        sticker1.setHabitat("Habitat 1");
        sticker1.setDescription("Desc 1");
        sticker1.setSrc("https://example.com/1.jpg");
        sticker1.setAlbumId(album.getId());
        Sticker s1 = stickerService.createSticker(sticker1);

        StickerRequest sticker2 = new StickerRequest();
        sticker2.setName("Sticker 2");
        sticker2.setLatinName("Latin 2");
        sticker2.setHabitat("Habitat 2");
        sticker2.setDescription("Desc 2");
        sticker2.setSrc("https://example.com/2.jpg");
        sticker2.setAlbumId(album.getId());
        Sticker s2 = stickerService.createSticker(sticker2);
        entityManager.flush();

        CollectorRequest collectorRequest = new CollectorRequest();
        collectorRequest.setUsername("RepeatCompleter");
        Collector collector = collectorService.createCollector(collectorRequest);

        // Give collector multiple sets of stickers
        Collector entity = collectorRepository.findById(collector.getId()).orElseThrow();
        entity.addSticker(s1.getId());
        entity.addSticker(s1.getId());
        entity.addSticker(s2.getId());
        entity.addSticker(s2.getId());
        collectorRepository.save(entity);

        // When - Complete twice
        albumService.completeAlbum(album.getId(), collector.getId());
        albumService.completeAlbum(album.getId(), collector.getId());

        // Then - Should have 2 stars and 2 completion records
        Collector updated = collectorRepository.findById(collector.getId()).orElseThrow();
        assertThat(updated.getStars()).isEqualTo(2);

        List<CompletedAlbum> completions = completedAlbumRepository.findAll();
        long count = completions.stream()
                .filter(ca -> ca.getCollector().getId().equals(collector.getId()) 
                        && ca.getAlbum().getId().equals(album.getId()))
                .count();
        assertThat(count).isEqualTo(2);
    }
}
