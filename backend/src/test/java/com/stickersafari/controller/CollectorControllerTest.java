package com.stickersafari.controller;

import com.stickersafari.dto.CollectorResponse;
import com.stickersafari.entity.Collector;
import com.stickersafari.mapper.CollectorMapper;
import com.stickersafari.service.CollectorService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CollectorController Tests")
class CollectorControllerTest {

    @Mock
    private CollectorService collectorService;
    
    @Mock
    private CollectorMapper collectorMapper;

    @InjectMocks
    private CollectorController collectorController;

    private Collector testCollector;
    private CollectorResponse testCollectorResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testCollector = new Collector();
        testCollector.setId(1L);
        testCollector.setUsername("Bob");
        testCollector.setOwnedStickers(new HashMap<>());
        
        testCollectorResponse = new CollectorResponse();
        testCollectorResponse.setId(1L);
        testCollectorResponse.setUsername("Bob");
    }

    @Test
    @DisplayName("New Feature: getCollectorByUsername endpoint should return collector with id field")
    void testGetCollectorByUsername() {
        // Given
        when(collectorService.findByUsername(anyString())).thenReturn(testCollector);
        when(collectorMapper.toResponse(testCollector)).thenReturn(testCollectorResponse);

        // When
        CollectorResponse response = collectorController.getCollectorByUsername("Bob");

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Bob", response.getUsername());
        verify(collectorService, times(1)).findByUsername("Bob");
        verify(collectorMapper, times(1)).toResponse(testCollector);
    }

    @Test
    @DisplayName("Bug Fix: CollectorResponse should include id field for frontend login")
    void testCollectorResponseIncludesId() {
        // Given
        when(collectorService.findByUsername(anyString())).thenReturn(testCollector);
        when(collectorMapper.toResponse(testCollector)).thenReturn(testCollectorResponse);

        // When
        CollectorResponse collectorResponse = collectorController.getCollectorByUsername("Bob");

        // Then
        assertNotNull(collectorResponse);
        assertNotNull(collectorResponse.getId(), "Id field should not be null");
        assertTrue(collectorResponse.getId() > 0, "Id should be a positive number");
    }

    @Test
    @DisplayName("getCollectorByUsername should throw exception for non-existent users")
    void testGetCollectorByUsernameNotFound() {
        // Given
        when(collectorService.findByUsername(anyString())).thenReturn(null);

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            collectorController.getCollectorByUsername("NonExistent");
        });
        verify(collectorService, times(1)).findByUsername("NonExistent");
    }

    @Test
    @DisplayName("Admin endpoint: Get paginated collectors without search")
    void testGetCollectorsPaginatedNoSearch() {
        // Given
        List<Collector> collectors = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Collector collector = new Collector();
            collector.setId((long) i);
            collector.setUsername("User" + i);
            collector.setOwnedStickers(new HashMap<>());
            collectors.add(collector);
        }
        
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Collector> collectorPage = new PageImpl<>(collectors, pageRequest, collectors.size());
        
        when(collectorService.getAllCollectorsWithPagination(any())).thenReturn(collectorPage);
        when(collectorMapper.toResponse(any(Collector.class))).thenAnswer(invocation -> {
            Collector collector = invocation.getArgument(0);
            CollectorResponse response = new CollectorResponse();
            response.setId(collector.getId());
            response.setUsername(collector.getUsername());
            return response;
        });

        // When
        ResponseEntity<Map<String, Object>> responseEntity = collectorController.getCollectorsPaginated(
            0, 10, "id", "asc", null
        );

        // Then
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        
        Map<String, Object> response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(0, response.get("currentPage"));
        assertEquals(5, response.get("totalItems"));
        assertEquals(1, response.get("totalPages"));
        
        @SuppressWarnings("unchecked")
        List<CollectorResponse> collectorResponses = (List<CollectorResponse>) response.get("collectors");
        assertEquals(5, collectorResponses.size());
        
        verify(collectorService, times(1)).getAllCollectorsWithPagination(any());
    }

    @Test
    @DisplayName("Admin endpoint: Search collectors by username")
    void testSearchCollectorsPaginated() {
        // Given
        String searchQuery = "Bob";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "username"));
        
        Collector bob = new Collector();
        bob.setId(1L);
        bob.setUsername("Bob");
        bob.setOwnedStickers(new HashMap<>());
        
        List<Collector> searchResults = List.of(bob);
        Page<Collector> collectorPage = new PageImpl<>(searchResults, pageRequest, searchResults.size());
        
        when(collectorService.searchCollectors(eq(searchQuery), any())).thenReturn(collectorPage);
        when(collectorMapper.toResponse(any(Collector.class))).thenAnswer(invocation -> {
            Collector collector = invocation.getArgument(0);
            CollectorResponse response = new CollectorResponse();
            response.setId(collector.getId());
            response.setUsername(collector.getUsername());
            return response;
        });

        // When
        ResponseEntity<Map<String, Object>> responseEntity = collectorController.getCollectorsPaginated(
            0, 10, "username", "asc", searchQuery
        );

        // Then
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        
        Map<String, Object> response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(1, response.get("totalItems"));
        
        @SuppressWarnings("unchecked")
        List<CollectorResponse> collectors = (List<CollectorResponse>) response.get("collectors");
        assertEquals(1, collectors.size());
        assertEquals("Bob", collectors.get(0).getUsername());
        
        verify(collectorService, times(1)).searchCollectors(eq(searchQuery), any());
    }

    @Test
    @DisplayName("Admin endpoint: Sort collectors descending")
    void testGetCollectorsPaginatedSortDescending() {
        // Given
        List<Collector> collectors = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Collector collector = new Collector();
            collector.setId((long) i);
            collector.setUsername("User" + i);
            collector.setOwnedStickers(new HashMap<>());
            collectors.add(collector);
        }
        
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "username"));
        Page<Collector> collectorPage = new PageImpl<>(collectors, pageRequest, collectors.size());
        
        when(collectorService.getAllCollectorsWithPagination(any())).thenReturn(collectorPage);
        when(collectorMapper.toResponse(any(Collector.class))).thenAnswer(invocation -> {
            Collector collector = invocation.getArgument(0);
            CollectorResponse response = new CollectorResponse();
            response.setId(collector.getId());
            response.setUsername(collector.getUsername());
            return response;
        });

        // When
        ResponseEntity<Map<String, Object>> responseEntity = collectorController.getCollectorsPaginated(
            0, 10, "username", "desc", null
        );

        // Then
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        
        verify(collectorService, times(1)).getAllCollectorsWithPagination(argThat(pageable -> 
            pageable.getSort().getOrderFor("username").getDirection() == Sort.Direction.DESC
        ));
    }

    @Test
    @DisplayName("Admin endpoint: Pagination with different page sizes")
    void testGetCollectorsPaginatedDifferentPageSizes() {
        // Given
        int pageSize = 5;
        List<Collector> collectors = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Collector collector = new Collector();
            collector.setId((long) i);
            collector.setUsername("User" + i);
            collector.setOwnedStickers(new HashMap<>());
            collectors.add(collector);
        }
        
        PageRequest pageRequest = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<Collector> collectorPage = new PageImpl<>(collectors, pageRequest, 10); // Total 10 items
        
        when(collectorService.getAllCollectorsWithPagination(any())).thenReturn(collectorPage);
        when(collectorMapper.toResponse(any(Collector.class))).thenAnswer(invocation -> {
            Collector collector = invocation.getArgument(0);
            CollectorResponse response = new CollectorResponse();
            response.setId(collector.getId());
            response.setUsername(collector.getUsername());
            return response;
        });

        // When
        ResponseEntity<Map<String, Object>> responseEntity = collectorController.getCollectorsPaginated(
            0, pageSize, "id", "asc", null
        );

        // Then
        assertNotNull(responseEntity);
        Map<String, Object> response = responseEntity.getBody();
        assertEquals(10, response.get("totalItems"));
        assertEquals(2, response.get("totalPages"));
        
        verify(collectorService, times(1)).getAllCollectorsWithPagination(argThat(pageable -> 
            pageable.getPageSize() == pageSize
        ));
    }
}
