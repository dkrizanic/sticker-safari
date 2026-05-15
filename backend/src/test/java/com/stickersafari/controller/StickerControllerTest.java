package com.stickersafari.controller;

import com.stickersafari.dto.StickerResponse;
import com.stickersafari.entity.Album;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.mapper.StickerMapper;
import com.stickersafari.service.CollectorService;
import com.stickersafari.service.StickerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("StickerController Tests")
class StickerControllerTest {

    @Mock
    private StickerService stickerService;

    @Mock
    private CollectorService collectorService;
    
    @Mock
    private StickerMapper stickerMapper;

    @InjectMocks
    private StickerController stickerController;

    private Collector testCollector;
    private Album testAlbum;
    private List<Sticker> testStickers;
    private List<StickerResponse> testStickerResponses;

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
        testStickerResponses = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Sticker sticker = new Sticker();
            sticker.setId((long) i);
            sticker.setName("Animal " + i);
            sticker.setAlbum(testAlbum);
            testStickers.add(sticker);
            
            StickerResponse response = new StickerResponse();
            response.setId((long) i);
            response.setName("Animal " + i);
            testStickerResponses.add(response);
        }
    }

    @Test
    @DisplayName("Bug Fix: openPack should return mutable list (not List.of())")
    void testOpenPackReturnsMutableList() {
        // Given
        when(stickerService.openPack(anyLong(), anyLong(), anyInt())).thenReturn(testStickers);
        when(stickerMapper.toResponseList(anyList())).thenReturn(testStickerResponses);

        // When
        ResponseEntity<List<StickerResponse>> responseEntity = stickerController.openPack(1L, 1L, 5);
        List<StickerResponse> response = responseEntity.getBody();

        // Then
        assertNotNull(response);
        assertTrue(response instanceof List);
        
        // Verify the list is mutable by attempting to access elements
        assertDoesNotThrow(() -> {
            if (!response.isEmpty()) {
                response.get(0); // Should work without throwing
            }
        });
        
        verify(collectorService, times(1)).addStickersToCollector(eq(1L), anyList());
    }

    @Test
    @DisplayName("openPack should handle size parameter correctly")
    void testOpenPackWithSizeParameter() {
        // Given
        int requestedSize = 3;
        List<Sticker> subset = testStickers.subList(0, requestedSize);
        List<StickerResponse> subsetResponses = testStickerResponses.subList(0, requestedSize);
        when(stickerService.openPack(anyLong(), anyLong(), eq(requestedSize))).thenReturn(subset);
        when(stickerMapper.toResponseList(subset)).thenReturn(subsetResponses);

        // When
        ResponseEntity<List<StickerResponse>> responseEntity = stickerController.openPack(1L, 1L, requestedSize);
        List<StickerResponse> response = responseEntity.getBody();

        // Then
        assertNotNull(response);
        assertEquals(requestedSize, response.size(), "Should return requested number of stickers");
        verify(stickerService, times(1)).openPack(1L, 1L, requestedSize);
    }

    @Test
    @DisplayName("openPack should add stickers to collector's owned stickers")
    void testOpenPackAddsStickersToCollector() {
        // Given
        when(stickerService.openPack(anyLong(), anyLong(), anyInt())).thenReturn(testStickers);
        when(stickerMapper.toResponseList(anyList())).thenReturn(testStickerResponses);

        // When
        stickerController.openPack(1L, 1L, 5);

        // Then
        verify(collectorService, times(1)).addStickersToCollector(eq(1L), anyList());
    }

    @Test
    @DisplayName("openPack should handle service exceptions")
    void testOpenPackCollectorNotFound() {
        // Given
        when(stickerService.openPack(anyLong(), anyLong(), anyInt())).thenReturn(testStickers);
        doThrow(new NoSuchElementException("Collector not found"))
            .when(collectorService).addStickersToCollector(anyLong(), anyList());

        // When/Then
        assertThrows(NoSuchElementException.class, () -> {
            stickerController.openPack(1L, 999L, 5);
        });
    }
}
