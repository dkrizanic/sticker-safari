package com.stickersafari.controller;

import com.stickersafari.dto.CollectorResponse;
import com.stickersafari.entity.Collector;
import com.stickersafari.service.CollectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("CollectorController Tests")
class CollectorControllerTest {

    @Mock
    private CollectorService collectorService;

    @InjectMocks
    private CollectorController collectorController;

    private Collector testCollector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testCollector = new Collector();
        testCollector.setId(1L);
        testCollector.setUsername("Bob");
        testCollector.setOwnedStickers(new HashMap<>());
    }

    @Test
    @DisplayName("New Feature: getCollectorByUsername endpoint should return collector with id field")
    void testGetCollectorByUsername() {
        // Given
        when(collectorService.findByUsername(anyString())).thenReturn(testCollector);

        // When
        CollectorResponse response = collectorController.getCollectorByUsername("Bob");

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId(), "CollectorResponse should include id field");
        assertEquals("Bob", response.getUsername());
        verify(collectorService, times(1)).findByUsername("Bob");
    }

    @Test
    @DisplayName("Bug Fix: CollectorResponse should include id field for frontend login")
    void testCollectorResponseIncludesId() {
        // Given
        when(collectorService.findByUsername(anyString())).thenReturn(testCollector);

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
}
