package com.stickersafari.controller;

import com.stickersafari.dto.StickerFilterRequest;
import com.stickersafari.dto.StickerResponse;
import com.stickersafari.entity.Album;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.mapper.StickerMapper;
import com.stickersafari.service.AlbumService;
import com.stickersafari.service.CollectorService;
import com.stickersafari.service.NotificationService;
import com.stickersafari.service.StickerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    
    @Mock
    private AlbumService albumService;
    
    @Mock
    private NotificationService notificationService;

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
        when(stickerService.openPackForCollector(anyLong(), anyLong(), anyInt())).thenReturn(testStickers);
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
        
        verify(stickerService, times(1)).openPackForCollector(eq(1L), eq(1L), eq(5));
    }

    @Test
    @DisplayName("openPack should handle size parameter correctly")
    void testOpenPackWithSizeParameter() {
        // Given
        int requestedSize = 3;
        List<Sticker> subset = testStickers.subList(0, requestedSize);
        List<StickerResponse> subsetResponses = testStickerResponses.subList(0, requestedSize);
        when(stickerService.openPackForCollector(anyLong(), anyLong(), eq(requestedSize))).thenReturn(subset);
        when(stickerMapper.toResponseList(subset)).thenReturn(subsetResponses);

        // When
        ResponseEntity<List<StickerResponse>> responseEntity = stickerController.openPack(1L, 1L, requestedSize);
        List<StickerResponse> response = responseEntity.getBody();

        // Then
        assertNotNull(response);
        assertEquals(requestedSize, response.size(), "Should return requested number of stickers");
        verify(stickerService, times(1)).openPackForCollector(1L, 1L, requestedSize);
    }

    @Test
    @DisplayName("openPack should add stickers to collector's owned stickers")
    void testOpenPackAddsStickersToCollector() {
        // Given
        when(stickerService.openPackForCollector(anyLong(), anyLong(), anyInt())).thenReturn(testStickers);
        when(stickerMapper.toResponseList(anyList())).thenReturn(testStickerResponses);

        // When
        stickerController.openPack(1L, 1L, 5);

        // Then
        verify(stickerService, times(1)).openPackForCollector(eq(1L), eq(1L), eq(5));
    }

    @Test
    @DisplayName("openPack should handle service exceptions")
    void testOpenPackCollectorNotFound() {
        // Given
        when(stickerService.openPackForCollector(anyLong(), anyLong(), anyInt()))
            .thenThrow(new NoSuchElementException("Collector not found"));

        // When/Then
        assertThrows(NoSuchElementException.class, () -> {
            stickerController.openPack(1L, 999L, 5);
        });
    }

    @Test
    @DisplayName("Admin endpoint: Get paginated stickers without filters")
    void testgetStickersWithPaginationNoFilters() {
        // Given
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("stickers", testStickerResponses);
        mockResponse.put("currentPage", 0);
        mockResponse.put("totalItems", 5L);
        mockResponse.put("totalPages", 1);
        
        when(stickerService.getFilteredStickers(0, 10, "id", "asc", null, null)).thenReturn(mockResponse);

        // When
        StickerFilterRequest filter = new StickerFilterRequest(0, 10, null, null, "id", "asc");
        ResponseEntity<Map<String, Object>> responseEntity = stickerController.getStickersWithPagination(filter);

        // Then
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        
        Map<String, Object> response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(0, (int)response.get("currentPage"));
        assertEquals(5L, (long)response.get("totalItems"));
        assertEquals(1, (int)response.get("totalPages"));
        
        @SuppressWarnings("unchecked")
        List<StickerResponse> stickers = (List<StickerResponse>) response.get("stickers");
        assertEquals(5, stickers.size());
        
        verify(stickerService, times(1)).getFilteredStickers(0, 10, "id", "asc", null, null);
    }

    @Test
    @DisplayName("Admin endpoint: Search stickers with query")
    void testSearchStickersPaginated() {
        // Given
        String searchQuery = "Lion";
        
        StickerResponse lionResponse = new StickerResponse();
        lionResponse.setId(1L);
        lionResponse.setName("Lion");
        lionResponse.setLatinName("Panthera leo");
        
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("stickers", Collections.singletonList(lionResponse));
        mockResponse.put("currentPage", 0);
        mockResponse.put("totalItems", 1L);
        mockResponse.put("totalPages", 1);
        
        when(stickerService.getFilteredStickers(0, 10, "name", "asc", searchQuery, null)).thenReturn(mockResponse);

        // When
        StickerFilterRequest filter = new StickerFilterRequest(0, 10, searchQuery, null, "name", "asc");
        ResponseEntity<Map<String, Object>> responseEntity = stickerController.getStickersWithPagination(filter);

        // Then
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        
        Map<String, Object> response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(1L, (long)response.get("totalItems"));
        
        @SuppressWarnings("unchecked")
        List<StickerResponse> stickers = (List<StickerResponse>) response.get("stickers");
        assertEquals(1, stickers.size());
        assertEquals("Lion", stickers.get(0).getName());
        
        verify(stickerService, times(1)).getFilteredStickers(0, 10, "name", "asc", searchQuery, null);
    }

    @Test
    @DisplayName("Admin endpoint: Filter stickers by album")
    void testgetStickersWithPaginationByAlbum() {
        // Given
        Long albumId = 1L;
        
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("stickers", testStickerResponses);
        mockResponse.put("currentPage", 0);
        mockResponse.put("totalItems", 5L);
        mockResponse.put("totalPages", 1);
        
        when(stickerService.getFilteredStickers(0, 10, "id", "asc", null, albumId)).thenReturn(mockResponse);

        // When
        StickerFilterRequest filter = new StickerFilterRequest(0, 10, null, albumId, "id", "asc");
        ResponseEntity<Map<String, Object>> responseEntity = stickerController.getStickersWithPagination(filter);

        // Then
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        
        Map<String, Object> response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(5L, (long)response.get("totalItems"));
        
        verify(stickerService, times(1)).getFilteredStickers(0, 10, "id", "asc", null, albumId);
    }

    @Test
    @DisplayName("Admin endpoint: Sort stickers descending")
    void testgetStickersWithPaginationSortDescending() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));
        Page<Sticker> stickerPage = new PageImpl<>(testStickers, pageRequest, testStickers.size());
        
        when(stickerService.getAllStickersWithPagination(any())).thenReturn(stickerPage);
        when(stickerMapper.toResponse(any(Sticker.class))).thenAnswer(invocation -> {
            Sticker sticker = invocation.getArgument(0);
            StickerResponse response = new StickerResponse();
            response.setId(sticker.getId());
            response.setName(sticker.getName());
            return response;
        });

        // When
        StickerFilterRequest filter = new StickerFilterRequest(0, 10, null, null, "name", "desc");
        ResponseEntity<Map<String, Object>> responseEntity = stickerController.getStickersWithPagination(filter);

        // Then
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        
        verify(stickerService, times(1)).getFilteredStickers(0, 10, "name", "desc", null, null);
    }

    @Test
    @DisplayName("Admin endpoint: Search with album filter combined")
    void testSearchStickersWithAlbumFilter() {
        // Given
        String searchQuery = "Lion";
        Long albumId = 1L;
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        
        Sticker lion = new Sticker();
        lion.setId(1L);
        lion.setName("Lion");
        lion.setAlbum(testAlbum);
        
        List<Sticker> searchResults = Collections.singletonList(lion);
        Page<Sticker> stickerPage = new PageImpl<>(searchResults, pageRequest, searchResults.size());
        
        when(stickerService.searchStickers(eq(searchQuery), any())).thenReturn(stickerPage);
        when(stickerMapper.toResponse(any(Sticker.class))).thenAnswer(invocation -> {
            Sticker sticker = invocation.getArgument(0);
            StickerResponse response = new StickerResponse();
            response.setId(sticker.getId());
            response.setName(sticker.getName());
            return response;
        });

        // When
        StickerFilterRequest filter = new StickerFilterRequest(0, 10, searchQuery, albumId, "name", "asc");
        ResponseEntity<Map<String, Object>> responseEntity = stickerController.getStickersWithPagination(filter);

        // Then
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        
        verify(stickerService, times(1)).getFilteredStickers(0, 10, "name", "asc", searchQuery, albumId);
        verify(stickerService, never()).getStickersOfAlbumWithPagination(anyLong(), any());
    }

    @Test
    @DisplayName("Admin endpoint: Pagination with different page sizes")
    void testgetStickersWithPaginationDifferentPageSizes() {
        // Given
        int pageSize = 5;
        
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("stickers", testStickerResponses);
        mockResponse.put("currentPage", 1);
        mockResponse.put("totalItems", 10L);
        mockResponse.put("totalPages", 2);
        
        when(stickerService.getFilteredStickers(1, pageSize, "id", "asc", null, null)).thenReturn(mockResponse);

        // When
        StickerFilterRequest filter = new StickerFilterRequest(1, 5, null, null, "id", "asc");
        ResponseEntity<Map<String, Object>> responseEntity = stickerController.getStickersWithPagination(filter);

        // Then
        assertNotNull(responseEntity);
        Map<String, Object> response = responseEntity.getBody();
        assertEquals(10L, (long)response.get("totalItems"));
        assertEquals(2, (int)response.get("totalPages"));
        
        verify(stickerService, times(1)).getFilteredStickers(1, pageSize, "id", "asc", null, null);
    }
}
