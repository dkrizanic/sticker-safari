package com.stickersafari.service;

import com.stickersafari.dto.AlbumCompletionResponse;
import com.stickersafari.dto.StickerResponse;
import com.stickersafari.entity.Album;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.exception.EntityNotFoundException;
import com.stickersafari.mapper.StickerMapper;
import com.stickersafari.repository.AlbumRepository;
import com.stickersafari.service.impl.AlbumServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceImplTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private CollectorService collectorService;

    @Mock
    private StickerMapper stickerMapper;

    @InjectMocks
    private AlbumServiceImpl albumService;

    private Album testAlbum;
    private Collector testCollector;
    private List<Sticker> testStickers;

    @BeforeEach
    void setUp() {
        testAlbum = new Album();
        testAlbum.setId(1L);
        testAlbum.setName("Test Album");

        testStickers = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Sticker sticker = new Sticker();
            sticker.setId((long) i);
            sticker.setName("Sticker " + i);
            sticker.setAlbum(testAlbum);
            testStickers.add(sticker);
        }
        testAlbum.setStickers(testStickers);

        testCollector = new Collector();
        testCollector.setId(1L);
        testCollector.setUsername("testuser");
        Map<Long, Integer> ownedStickers = new HashMap<>();
        ownedStickers.put(1L, 1);
        ownedStickers.put(2L, 2);
        testCollector.setOwnedStickers(ownedStickers);
    }

    @Test
    void getAlbum_ShouldReturnAlbum_WhenAlbumExists() {
        // Given
        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));

        // When
        Album result = albumService.getAlbum(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Album");
        verify(albumRepository).findById(1L);
    }

    @Test
    void getAlbum_ShouldThrowException_WhenAlbumDoesNotExist() {
        // Given
        when(albumRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> albumService.getAlbum(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Album not found with ID: 99");
        verify(albumRepository).findById(99L);
    }

    @Test
    void getMissingStickers_ShouldReturnMissingStickers() {
        // Given
        when(collectorService.findById(1L)).thenReturn(testCollector);

        // When
        List<Sticker> result = albumService.getMissingStickers(testAlbum, 1L);

        // Then - Collector owns stickers 1, 2, so missing are 3, 4, 5
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Sticker::getId).containsExactlyInAnyOrder(3L, 4L, 5L);
        verify(collectorService).findById(1L);
    }

    @Test
    void getAlbumCompletion_ShouldCalculateCorrectPercentage() {
        // Given
        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));
        when(collectorService.findById(1L)).thenReturn(testCollector);
        
        List<StickerResponse> stickerResponses = new ArrayList<>();
        when(stickerMapper.toResponseList(any())).thenReturn(stickerResponses);

        // When
        AlbumCompletionResponse result = albumService.getAlbumCompletion(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCompletion()).isCloseTo(40.0f, offset(0.01f)); // 2 owned out of 5 = 40% complete
        verify(albumRepository).findById(1L);
        verify(collectorService).findById(1L);
        verify(stickerMapper).toResponseList(any());
    }

    @Test
    void getAlbumCompletion_ShouldReturn100_WhenAllStickersOwned() {
        // Given
        Map<Long, Integer> allStickers = new HashMap<>();
        for (Sticker sticker : testStickers) {
            allStickers.put(sticker.getId(), 1);
        }
        testCollector.setOwnedStickers(allStickers);

        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));
        when(collectorService.findById(1L)).thenReturn(testCollector);
        when(stickerMapper.toResponseList(any())).thenReturn(new ArrayList<>());

        // When
        AlbumCompletionResponse result = albumService.getAlbumCompletion(1L, 1L);

        // Then
        assertThat(result.getCompletion()).isEqualTo(100.0f); // All stickers owned = 100% complete
    }

    @Test
    void getAlbumCompletion_ShouldReturn0_WhenNoStickersOwned() {
        // Given
        testCollector.setOwnedStickers(new HashMap<>());

        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));
        when(collectorService.findById(1L)).thenReturn(testCollector);
        when(stickerMapper.toResponseList(any())).thenReturn(new ArrayList<>());

        // When
        AlbumCompletionResponse result = albumService.getAlbumCompletion(1L, 1L);

        // Then
        assertThat(result.getCompletion()).isEqualTo(0.0f); // No stickers owned = 0% complete
    }
}
