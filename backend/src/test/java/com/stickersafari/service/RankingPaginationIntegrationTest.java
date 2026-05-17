package com.stickersafari.service;

import com.stickersafari.dto.PagedRankingResponse;
import com.stickersafari.dto.RankingEntry;
import com.stickersafari.entity.Album;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.CompletedAlbum;
import com.stickersafari.repository.AlbumRepository;
import com.stickersafari.repository.CollectorRepository;
import com.stickersafari.repository.CompletedAlbumRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Ranking Pagination and Search Tests")
class RankingPaginationIntegrationTest {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private CollectorRepository collectorRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private CompletedAlbumRepository completedAlbumRepository;

    private Album album;

    @BeforeEach
    void setUp() {
        // Create test album
        album = new Album();
        album.setName("Test Album");
        album = albumRepository.save(album);

        // Create multiple collectors with varied data
        createCollectorWithCompletions("Alice", 3, 2); // 3 stars, 2 completions = 17 points
        createCollectorWithCompletions("Bob", 2, 5);   // 2 stars, 5 completions = 15 points
        createCollectorWithCompletions("Charlie", 1, 8); // 1 star, 8 completions = 13 points
        createCollectorWithCompletions("David", 0, 10);  // 0 stars, 10 completions = 10 points
        createCollectorWithCompletions("Eve", 0, 5);     // 0 stars, 5 completions = 5 points
        createCollectorWithCompletions("Frank", 0, 3);   // 0 stars, 3 completions = 3 points
        createCollectorWithCompletions("Grace", 0, 2);   // 0 stars, 2 completions = 2 points
        createCollectorWithCompletions("Henry", 0, 1);   // 0 stars, 1 completion = 1 point
    }

    private void createCollectorWithCompletions(String username, int stars, int completions) {
        Collector collector = new Collector();
        collector.setUsername(username);
        collector.setStars(stars);
        collector = collectorRepository.save(collector);

        // Create completion records
        for (int i = 0; i < completions; i++) {
            CompletedAlbum completion = new CompletedAlbum();
            completion.setCollector(collector);
            completion.setAlbum(album);
            completion.setCompletedAt(LocalDateTime.now().minusDays(i));
            completedAlbumRepository.save(completion);
        }
    }

    @Test
    @DisplayName("Should return first page with correct page size")
    void testPaginationFirstPage() {
        // When
        PagedRankingResponse response = rankingService.getAllTimeRanking(0, 3, null);

        // Then
        assertThat(response.getRankings()).hasSize(3);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(3);
        assertThat(response.getTotalItems()).isGreaterThanOrEqualTo(8);
        assertThat(response.getTotalPages()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should return second page with correct data")
    void testPaginationSecondPage() {
        // When
        PagedRankingResponse page1 = rankingService.getAllTimeRanking(0, 3, null);
        PagedRankingResponse page2 = rankingService.getAllTimeRanking(1, 3, null);

        // Then
        assertThat(page2.getRankings()).hasSize(3);
        assertThat(page2.getCurrentPage()).isEqualTo(1);
        
        // Verify different data on different pages
        List<Long> page1Ids = page1.getRankings().stream()
                .map(RankingEntry::getCollectorId)
                .toList();
        List<Long> page2Ids = page2.getRankings().stream()
                .map(RankingEntry::getCollectorId)
                .toList();
        
        assertThat(page1Ids).doesNotContainAnyElementsOf(page2Ids);
    }

    @Test
    @DisplayName("Should return last page with remaining items")
    void testPaginationLastPage() {
        // When - Get last page (8 items total, 3 per page = 3 pages, last page has 2 items)
        PagedRankingResponse response = rankingService.getAllTimeRanking(2, 3, null);

        // Then
        assertThat(response.getRankings()).hasSizeLessThanOrEqualTo(3);
        assertThat(response.getCurrentPage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should search by username case-insensitive")
    void testSearchByUsername() {
        // When - Search for "alice" (lowercase)
        PagedRankingResponse response = rankingService.getAllTimeRanking(0, 10, "alice");

        // Then
        assertThat(response.getRankings()).hasSize(1);
        assertThat(response.getRankings().get(0).getUsername()).isEqualToIgnoringCase("Alice");
        assertThat(response.getTotalItems()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should search by partial username")
    void testSearchByPartialUsername() {
        // When - Search for "a" - should find "Alice", "Charlie", "David", "Grace", "Frank"
        PagedRankingResponse response = rankingService.getAllTimeRanking(0, 10, "a");

        // Then
        assertThat(response.getTotalItems()).isGreaterThanOrEqualTo(4);
        assertThat(response.getRankings().stream()
                .allMatch(r -> r.getUsername().toLowerCase().contains("a")))
                .isTrue();
    }

    @Test
    @DisplayName("Should return empty results for non-matching search")
    void testSearchNoResults() {
        // When
        PagedRankingResponse response = rankingService.getAllTimeRanking(0, 10, "XYZ123");

        // Then
        assertThat(response.getRankings()).isEmpty();
        assertThat(response.getTotalItems()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should combine search and pagination")
    void testSearchWithPagination() {
        // Given - Create more collectors with similar names
        for (int i = 1; i <= 15; i++) {
            createCollectorWithCompletions("TestUser" + i, 0, i);
        }

        // When - Search with pagination
        PagedRankingResponse page1 = rankingService.getAllTimeRanking(0, 5, "TestUser");
        PagedRankingResponse page2 = rankingService.getAllTimeRanking(1, 5, "TestUser");

        // Then
        assertThat(page1.getRankings()).hasSize(5);
        assertThat(page2.getRankings()).hasSize(5);
        assertThat(page1.getTotalItems()).isEqualTo(15);
        assertThat(page1.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should calculate correct total pages")
    void testTotalPagesCalculation() {
        // When - 8 items, 3 per page = 3 pages
        PagedRankingResponse response = rankingService.getAllTimeRanking(0, 3, null);

        // Then
        assertThat(response.getTotalPages()).isEqualTo(
                (int) Math.ceil((double) response.getTotalItems() / 3)
        );
    }

    @Test
    @DisplayName("Should order by score descending (stars*5 + completions)")
    void testOrderByScoreDescending() {
        // When
        PagedRankingResponse response = rankingService.getAllTimeRanking(0, 10, null);

        // Then - Verify ordering
        List<RankingEntry> rankings = response.getRankings();
        for (int i = 0; i < rankings.size() - 1; i++) {
            long currentScore = rankings.get(i).getScore();
            long nextScore = rankings.get(i + 1).getScore();
            assertThat(currentScore).isGreaterThanOrEqualTo(nextScore);
        }

        // Verify Alice is first (3*5 + 2 = 17 points)
        assertThat(rankings.get(0).getUsername()).isEqualTo("Alice");
        assertThat(rankings.get(0).getScore()).isEqualTo(17);
    }

    @Test
    @DisplayName("Should handle monthly ranking with search")
    void testMonthlyRankingWithSearch() {
        // Given - Create completions in current month
        LocalDateTime now = LocalDateTime.now();
        
        Collector searchable = new Collector();
        searchable.setUsername("MonthlyUser");
        searchable.setStars(0);
        searchable = collectorRepository.save(searchable);

        CompletedAlbum completion = new CompletedAlbum();
        completion.setCollector(searchable);
        completion.setAlbum(album);
        completion.setCompletedAt(now);
        completedAlbumRepository.save(completion);

        // When
        PagedRankingResponse response = rankingService.getMonthlyRanking(
                now.getYear(), 
                now.getMonthValue(), 
                0, 
                10, 
                "MonthlyUser"
        );

        // Then
        assertThat(response.getRankings()).hasSize(1);
        assertThat(response.getRankings().get(0).getUsername()).isEqualTo("MonthlyUser");
    }

    @Test
    @DisplayName("Should handle yearly ranking with pagination")
    void testYearlyRankingWithPagination() {
        // Given - Create completions in current year
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 1; i <= 12; i++) {
            Collector collector = new Collector();
            collector.setUsername("YearlyCollector" + i);
            collector.setStars(0);
            collector = collectorRepository.save(collector);

            CompletedAlbum completion = new CompletedAlbum();
            completion.setCollector(collector);
            completion.setAlbum(album);
            completion.setCompletedAt(now.minusMonths(i));
            completedAlbumRepository.save(completion);
        }

        // When
        PagedRankingResponse page1 = rankingService.getYearlyRanking(now.getYear(), 0, 5, null);
        PagedRankingResponse page2 = rankingService.getYearlyRanking(now.getYear(), 1, 5, null);

        // Then
        assertThat(page1.getRankings()).hasSize(5);
        assertThat(page2.getRankings()).hasSize(5);
        assertThat(page1.getTotalItems()).isGreaterThanOrEqualTo(12);
    }

    @Test
    @DisplayName("Should handle empty database gracefully")
    void testEmptyDatabasePagination() {
        // Given - Clear all data
        completedAlbumRepository.deleteAll();
        collectorRepository.deleteAll();

        // When
        PagedRankingResponse response = rankingService.getAllTimeRanking(0, 10, null);

        // Then
        assertThat(response.getRankings()).isEmpty();
        assertThat(response.getTotalItems()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should handle page size larger than total items")
    void testPageSizeLargerThanTotal() {
        // When - Request 100 items per page when only 8 exist
        PagedRankingResponse response = rankingService.getAllTimeRanking(0, 100, null);

        // Then
        assertThat(response.getRankings()).hasSizeLessThanOrEqualTo(100);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getCurrentPage()).isEqualTo(0);
    }
}
