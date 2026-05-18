package com.stickersafari.controller;

import com.stickersafari.dto.PagedRankingResponse;
import com.stickersafari.dto.RankingEntry;
import com.stickersafari.dto.RankingFilterRequest;
import com.stickersafari.service.RankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Ranking Controller Tests")
class RankingControllerPaginationTest {

    @Mock
    private RankingService rankingService;

    @InjectMocks
    private RankingController rankingController;

    @Test
    @DisplayName("GET /rankings/all-time should return paged results")
    void testGetAllTimeRankingWithPagination() {
        // Given
        RankingEntry entry1 = new RankingEntry(1L, "Alice", 2L, 3L, 17L);
        RankingEntry entry2 = new RankingEntry(2L, "Bob", 5L, 2L, 15L);
        
        PagedRankingResponse response = new PagedRankingResponse(
                Arrays.asList(entry1, entry2),
                0,  // currentPage
                3,  // totalPages
                25, // totalItems
                10  // pageSize
        );

        when(rankingService.getRankingByType("all-time", null, null, 0, 10, null)).thenReturn(response);

        // When
        RankingFilterRequest filter = new RankingFilterRequest(0, 10, null);
        ResponseEntity<PagedRankingResponse> result = rankingController.getAllTimeRanking(filter);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getRankings()).hasSize(2);
        assertThat(result.getBody().getCurrentPage()).isEqualTo(0);
        assertThat(result.getBody().getTotalPages()).isEqualTo(3);
        assertThat(result.getBody().getTotalItems()).isEqualTo(25);
        assertThat(result.getBody().getPageSize()).isEqualTo(10);
        assertThat(result.getBody().getRankings().get(0).getUsername()).isEqualTo("Alice");
        assertThat(result.getBody().getRankings().get(0).getStars()).isEqualTo(3);
        assertThat(result.getBody().getRankings().get(0).getCompletions()).isEqualTo(2);
        assertThat(result.getBody().getRankings().get(0).getScore()).isEqualTo(17);

        verify(rankingService).getRankingByType("all-time", null, null, 0, 10, null);
    }

    @Test
    @DisplayName("GET /rankings/all-time with search parameter")
    void testGetAllTimeRankingWithSearch() {
        // Given
        RankingEntry entry = new RankingEntry(1L, "Alice", 2L, 3L, 17L);
        
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.singletonList(entry),
                0, 1, 1, 10
        );

        when(rankingService.getRankingByType("all-time", null, null, 0, 10, "Alice")).thenReturn(response);

        // When
        RankingFilterRequest filter = new RankingFilterRequest(0, 10, "Alice");
        ResponseEntity<PagedRankingResponse> result = rankingController.getAllTimeRanking(filter);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getRankings()).hasSize(1);
        assertThat(result.getBody().getTotalItems()).isEqualTo(1);
        assertThat(result.getBody().getRankings().get(0).getUsername()).isEqualTo("Alice");

        verify(rankingService).getRankingByType("all-time", null, null, 0, 10, "Alice");
    }

    @Test
    @DisplayName("GET /rankings/all-time should use default pagination parameters")
    void testGetAllTimeRankingWithDefaults() {
        // Given
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.emptyList(),
                0, 1, 0, 10
        );

        when(rankingService.getRankingByType("all-time", null, null, 0, 10, null)).thenReturn(response);

        // When
        RankingFilterRequest filter = new RankingFilterRequest(0, 10, null);
        ResponseEntity<PagedRankingResponse> result = rankingController.getAllTimeRanking(filter);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCurrentPage()).isEqualTo(0);
        assertThat(result.getBody().getPageSize()).isEqualTo(10);

        verify(rankingService).getRankingByType("all-time", null, null, 0, 10, null);
    }

    @Test
    @DisplayName("GET /rankings/monthly/{year}/{month} with pagination")
    void testGetMonthlyRankingWithPagination() {
        // Given
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.emptyList(),
                0, 1, 0, 10
        );

        when(rankingService.getRankingByType("monthly", 2026, 5, 0, 10, null)).thenReturn(response);

        // When
        RankingFilterRequest filter = new RankingFilterRequest(0, 10, null);
        ResponseEntity<PagedRankingResponse> result = rankingController.getMonthlyRanking(2026, 5, filter);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCurrentPage()).isEqualTo(0);

        verify(rankingService).getRankingByType("monthly", 2026, 5, 0, 10, null);
    }

    @Test
    @DisplayName("GET /rankings/monthly/{year}/{month} with search")
    void testGetMonthlyRankingWithSearch() {
        // Given
        RankingEntry entry = new RankingEntry(1L, "Bob", 5L, 0L, 5L);
        
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.singletonList(entry),
                0, 1, 1, 10
        );

        when(rankingService.getRankingByType("monthly", 2026, 5, 0, 10, "Bob")).thenReturn(response);

        // When
        RankingFilterRequest filter = new RankingFilterRequest(0, 10, "Bob");
        ResponseEntity<PagedRankingResponse> result = rankingController.getMonthlyRanking(2026, 5, filter);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getRankings().get(0).getUsername()).isEqualTo("Bob");

        verify(rankingService).getRankingByType("monthly", 2026, 5, 0, 10, "Bob");
    }

    @Test
    @DisplayName("GET /rankings/yearly/{year} with pagination")
    void testGetYearlyRankingWithPagination() {
        // Given
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.emptyList(),
                1, 5, 45, 10
        );

        when(rankingService.getRankingByType("yearly", 2026, null, 1, 10, null)).thenReturn(response);

        // When
        RankingFilterRequest filter = new RankingFilterRequest(1, 10, null);
        ResponseEntity<PagedRankingResponse> result = rankingController.getYearlyRanking(2026, filter);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCurrentPage()).isEqualTo(1);
        assertThat(result.getBody().getTotalPages()).isEqualTo(5);
        assertThat(result.getBody().getTotalItems()).isEqualTo(45);

        verify(rankingService).getRankingByType("yearly", 2026, null, 1, 10, null);
    }

    @Test
    @DisplayName("GET /rankings/yearly/{year} with search")
    void testGetYearlyRankingWithSearch() {
        // Given
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.emptyList(),
                0, 1, 0, 10
        );

        when(rankingService.getRankingByType("yearly", 2026, null, 0, 10, "Test")).thenReturn(response);

        // When
        RankingFilterRequest filter = new RankingFilterRequest(0, 10, "Test");
        ResponseEntity<PagedRankingResponse> result = rankingController.getYearlyRanking(2026, filter);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);

        verify(rankingService).getRankingByType("yearly", 2026, null, 0, 10, "Test");
    }

    @Test
    @DisplayName("Should handle empty search results")
    void testEmptySearchResults() {
        // Given
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.emptyList(),
                0, 0, 0, 10
        );

        when(rankingService.getRankingByType("all-time", null, null, 0, 10, "NonExistent")).thenReturn(response);

        // When
        RankingFilterRequest filter = new RankingFilterRequest(0, 10, "NonExistent");
        ResponseEntity<PagedRankingResponse> result = rankingController.getAllTimeRanking(filter);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getRankings()).isEmpty();
        assertThat(result.getBody().getTotalItems()).isEqualTo(0);
        assertThat(result.getBody().getTotalPages()).isEqualTo(0);

        verify(rankingService).getRankingByType("all-time", null, null, 0, 10, "NonExistent");
    }

    @Test
    @DisplayName("Should handle custom page sizes")
    void testCustomPageSize() {
        // Given
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.emptyList(),
                0, 1, 20, 20
        );

        when(rankingService.getRankingByType("all-time", null, null, 0, 20, null)).thenReturn(response);

        // When
        RankingFilterRequest filter = new RankingFilterRequest(0, 20, null);
        ResponseEntity<PagedRankingResponse> result = rankingController.getAllTimeRanking(filter);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getPageSize()).isEqualTo(20);

        verify(rankingService).getRankingByType("all-time", null, null, 0, 20, null);
    }

    @Test
    @DisplayName("Should handle large page numbers")
    void testLargePageNumber() {
        // Given
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.emptyList(),
                99, 100, 1000, 10
        );

        when(rankingService.getRankingByType("all-time", null, null, 99, 10, null)).thenReturn(response);

        // When
        RankingFilterRequest filter = new RankingFilterRequest(99, 10, null);
        ResponseEntity<PagedRankingResponse> result = rankingController.getAllTimeRanking(filter);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCurrentPage()).isEqualTo(99);
        assertThat(result.getBody().getTotalPages()).isEqualTo(100);

        verify(rankingService).getRankingByType("all-time", null, null, 99, 10, null);
    }
}
