package com.stickersafari.controller;

import com.stickersafari.dto.PagedRankingResponse;
import com.stickersafari.dto.RankingEntry;
import com.stickersafari.service.RankingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RankingController.class)
@DisplayName("Ranking Controller Tests")
class RankingControllerPaginationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RankingService rankingService;

    @Test
    @WithMockUser
    @DisplayName("GET /rankings/all-time should return paged results")
    void testGetAllTimeRankingWithPagination() throws Exception {
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

        when(rankingService.getAllTimeRanking(0, 10, null)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/rankings/all-time")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rankings").isArray())
                .andExpect(jsonPath("$.rankings.length()").value(2))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.totalItems").value(25))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.rankings[0].username").value("Alice"))
                .andExpect(jsonPath("$.rankings[0].stars").value(3))
                .andExpect(jsonPath("$.rankings[0].completions").value(2))
                .andExpect(jsonPath("$.rankings[0].score").value(17));

        verify(rankingService).getAllTimeRanking(0, 10, null);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /rankings/all-time with search parameter")
    void testGetAllTimeRankingWithSearch() throws Exception {
        // Given
        RankingEntry entry = new RankingEntry(1L, "Alice", 2L, 3L, 17L);
        
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.singletonList(entry),
                0, 1, 1, 10
        );

        when(rankingService.getAllTimeRanking(0, 10, "Alice")).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/rankings/all-time")
                        .param("page", "0")
                        .param("size", "10")
                        .param("search", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rankings.length()").value(1))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.rankings[0].username").value("Alice"));

        verify(rankingService).getAllTimeRanking(0, 10, "Alice");
    }

    @Test
    @WithMockUser
    @DisplayName("GET /rankings/all-time should use default pagination parameters")
    void testGetAllTimeRankingWithDefaults() throws Exception {
        // Given
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.emptyList(),
                0, 1, 0, 10
        );

        when(rankingService.getAllTimeRanking(0, 10, null)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/rankings/all-time"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));

        verify(rankingService).getAllTimeRanking(0, 10, null);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /rankings/monthly/{year}/{month} with pagination")
    void testGetMonthlyRankingWithPagination() throws Exception {
        // Given
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.emptyList(),
                0, 1, 0, 10
        );

        when(rankingService.getMonthlyRanking(2026, 5, 0, 10, null)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/rankings/monthly/2026/5")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0));

        verify(rankingService).getMonthlyRanking(2026, 5, 0, 10, null);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /rankings/monthly/{year}/{month} with search")
    void testGetMonthlyRankingWithSearch() throws Exception {
        // Given
        RankingEntry entry = new RankingEntry(1L, "Bob", 5L, 0L, 5L);
        
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.singletonList(entry),
                0, 1, 1, 10
        );

        when(rankingService.getMonthlyRanking(2026, 5, 0, 10, "Bob")).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/rankings/monthly/2026/5")
                        .param("page", "0")
                        .param("size", "10")
                        .param("search", "Bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rankings[0].username").value("Bob"));

        verify(rankingService).getMonthlyRanking(2026, 5, 0, 10, "Bob");
    }

    @Test
    @WithMockUser
    @DisplayName("GET /rankings/yearly/{year} with pagination")
    void testGetYearlyRankingWithPagination() throws Exception {
        // Given
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.emptyList(),
                1, 5, 45, 10
        );

        when(rankingService.getYearlyRanking(2026, 1, 10, null)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/rankings/yearly/2026")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalPages").value(5))
                .andExpect(jsonPath("$.totalItems").value(45));

        verify(rankingService).getYearlyRanking(2026, 1, 10, null);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /rankings/yearly/{year} with search")
    void testGetYearlyRankingWithSearch() throws Exception {
        // Given
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.emptyList(),
                0, 1, 0, 10
        );

        when(rankingService.getYearlyRanking(2026, 0, 10, "Test")).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/rankings/yearly/2026")
                        .param("search", "Test"))
                .andExpect(status().isOk());

        verify(rankingService).getYearlyRanking(2026, 0, 10, "Test");
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle empty search results")
    void testEmptySearchResults() throws Exception {
        // Given
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.emptyList(),
                0, 0, 0, 10
        );

        when(rankingService.getAllTimeRanking(0, 10, "NonExistent")).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/rankings/all-time")
                        .param("search", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rankings").isEmpty())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(rankingService).getAllTimeRanking(0, 10, "NonExistent");
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle custom page sizes")
    void testCustomPageSize() throws Exception {
        // Given
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.emptyList(),
                0, 1, 20, 20
        );

        when(rankingService.getAllTimeRanking(0, 20, null)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/rankings/all-time")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(20));

        verify(rankingService).getAllTimeRanking(0, 20, null);
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle large page numbers")
    void testLargePageNumber() throws Exception {
        // Given
        PagedRankingResponse response = new PagedRankingResponse(
                Collections.emptyList(),
                99, 100, 1000, 10
        );

        when(rankingService.getAllTimeRanking(99, 10, null)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/rankings/all-time")
                        .param("page", "99")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(99))
                .andExpect(jsonPath("$.totalPages").value(100));

        verify(rankingService).getAllTimeRanking(99, 10, null);
    }
}
