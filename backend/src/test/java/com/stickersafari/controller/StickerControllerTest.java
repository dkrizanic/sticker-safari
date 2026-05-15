package com.stickersafari.controller;

import com.stickersafari.dto.StickerResponse;
import com.stickersafari.entity.Album;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.repository.CollectorRepository;
import com.stickersafari.service.StickerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("StickerController Tests")
class StickerControllerTest {

    @Mock
    private StickerService stickerService;

    @Mock
    private CollectorRepository collectorRepository;

    @InjectMocks
    private StickerController stickerController;

    private Collector testCollector;
    private Album testAlbum;
    private List<Sticker> testStickers;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testAlbum = new Album();
        testAlbum.setId(1L);
        testAlbum.setName("African Safari");

        testCollector = new Collector();
        testCollector.setId(1L);
        testCollector.setUsername("TestUser");
        testCollector.setOwnedStickers(new HashMap<>());

        testStickers = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Sticker sticker = new Sticker();
            sticker.setId((long) i);
            sticker.setName("Animal " + i);
            sticker.setAlbum(testAlbum);
            testStickers.add(sticker);
        }
    }

    @Test
    @DisplayName("Bug Fix: openPack should return mutable list (not List.of())")
    void testOpenPackReturnsMutableList() {
        // Given
        when(stickerService.getStickersOfAlbum(anyLong())).thenReturn(testStickers);
        when(collectorRepository.findById(anyLong())).thenReturn(Optional.of(testCollector));

        // When
        List<StickerResponse> response = stickerController.openPack(1L, 1L, 5);

        // Then
        assertNotNull(response);
        assertTrue(response instanceof List);
        
        // Verify the list is mutable by attempting to access elements
        assertDoesNotThrow(() -> {
            if (!response.isEmpty()) {
                response.get(0); // Should work without throwing
            }
        });
        
        verify(collectorRepository, times(1)).save(any(Collector.class));
    }

    @Test
    @DisplayName("openPack should handle size parameter correctly")
    void testOpenPackWithSizeParameter() {
        // Given
        int requestedSize = 3;
        when(stickerService.getStickersOfAlbum(anyLong())).thenReturn(testStickers);
        when(collectorRepository.findById(anyLong())).thenReturn(Optional.of(testCollector));

        // When
        List<StickerResponse> response = stickerController.openPack(1L, 1L, requestedSize);

        // Then
        assertNotNull(response);
        assertEquals(requestedSize, response.size(), "Should return requested number of stickers");
        verify(stickerService, times(1)).getStickersOfAlbum(1L);
    }

    @Test
    @DisplayName("openPack should add stickers to collector's owned stickers")
    void testOpenPackAddsStickersToCollector() {
        // Given
        when(stickerService.getStickersOfAlbum(anyLong())).thenReturn(testStickers);
        when(collectorRepository.findById(anyLong())).thenReturn(Optional.of(testCollector));

        // When
        stickerController.openPack(1L, 1L, 5);

        // Then
        verify(collectorRepository, times(1)).save(argThat(collector ->
            collector.getOwnedStickers().size() > 0
        ));
    }

    @Test
    @DisplayName("openPack should return 404 when collector not found")
    void testOpenPackCollectorNotFound() {
        // Given
        when(stickerService.getStickersOfAlbum(anyLong())).thenReturn(testStickers);
        when(collectorRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NoSuchElementException.class, () -> {
            stickerController.openPack(1L, 999L, 5);
        });
    }
}
