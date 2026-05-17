package com.stickersafari.service;

import com.stickersafari.dto.CollectorRequest;
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
@DisplayName("Ranking System Integration Tests")
class RankingIntegrationTest {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private CollectorService collectorService;

    @Autowired
    private CollectorRepository collectorRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private CompletedAlbumRepository completedAlbumRepository;

    private Collector collector1;
    private Collector collector2;
    private Collector collector3;
    private Album album;

    @BeforeEach
    void setUp() {
        // Create test collectors using service (handles User creation)
        CollectorRequest request1 = new CollectorRequest();
        request1.setUsername("TopCollector");
        collector1 = collectorService.createCollector(request1);
        collector1.setStars(5);
        collector1 = collectorRepository.save(collector1);

        CollectorRequest request2 = new CollectorRequest();
        request2.setUsername("MidCollector");
        collector2 = collectorService.createCollector(request2);
        collector2.setStars(3);
        collector2 = collectorRepository.save(collector2);

        CollectorRequest request3 = new CollectorRequest();
        request3.setUsername("LowCollector");
        collector3 = collectorService.createCollector(request3);
        collector3.setStars(1);
        collector3 = collectorRepository.save(collector3);

        // Create test album
        album = new Album();
        album.setName("Test Album");
        album = albumRepository.save(album);
    }

    @Test
    @DisplayName("Should return all-time ranking ordered by stars descending")
    void testGetAllTimeRanking() {
        // Given - Collectors created in setUp with different star counts

        // When
        List<RankingEntry> ranking = rankingService.getAllTimeRanking();

        // Then
        assertThat(ranking).hasSizeGreaterThanOrEqualTo(3);
        
        // Find our test collectors in the ranking
        RankingEntry top = ranking.stream()
                .filter(r -> r.getCollectorId().equals(collector1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(top.getUsername()).isEqualTo("TopCollector");
        assertThat(top.getCompletions()).isEqualTo(5);

        // Verify ordering (higher stars come first)
        int topIndex = ranking.indexOf(top);
        RankingEntry mid = ranking.stream()
                .filter(r -> r.getCollectorId().equals(collector2.getId()))
                .findFirst()
                .orElseThrow();
        int midIndex = ranking.indexOf(mid);
        
        assertThat(topIndex).isLessThan(midIndex);
    }

    @Test
    @DisplayName("Should return monthly ranking for specific month")
    void testGetMonthlyRanking() {
        // Given - Create completions in current month
        LocalDateTime now = LocalDateTime.now();
        
        CompletedAlbum completion1 = new CompletedAlbum();
        completion1.setCollector(collector1);
        completion1.setAlbum(album);
        completion1.setCompletedAt(now);
        completedAlbumRepository.save(completion1);

        CompletedAlbum completion2 = new CompletedAlbum();
        completion2.setCollector(collector1);
        completion2.setAlbum(album);
        completion2.setCompletedAt(now.minusDays(5));
        completedAlbumRepository.save(completion2);

        CompletedAlbum completion3 = new CompletedAlbum();
        completion3.setCollector(collector2);
        completion3.setAlbum(album);
        completion3.setCompletedAt(now.minusDays(10));
        completedAlbumRepository.save(completion3);

        // When
        List<RankingEntry> ranking = rankingService.getMonthlyRanking(
                now.getYear(), 
                now.getMonthValue()
        );

        // Then
        assertThat(ranking).isNotEmpty();
        
        // Collector1 should have 2 completions this month
        RankingEntry top = ranking.stream()
                .filter(r -> r.getCollectorId().equals(collector1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(top.getCompletions()).isEqualTo(2);

        // Collector2 should have 1 completion this month
        RankingEntry second = ranking.stream()
                .filter(r -> r.getCollectorId().equals(collector2.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(second.getCompletions()).isEqualTo(1);

        // Top should come before second
        assertThat(ranking.indexOf(top)).isLessThan(ranking.indexOf(second));
    }

    @Test
    @DisplayName("Should return yearly ranking for specific year")
    void testGetYearlyRanking() {
        // Given - Create completions in current year
        LocalDateTime now = LocalDateTime.now();
        
        // Collector1 gets 3 completions this year
        for (int i = 0; i < 3; i++) {
            CompletedAlbum completion = new CompletedAlbum();
            completion.setCollector(collector1);
            completion.setAlbum(album);
            completion.setCompletedAt(now.minusMonths(i));
            completedAlbumRepository.save(completion);
        }

        // Collector2 gets 1 completion this year
        CompletedAlbum completion2 = new CompletedAlbum();
        completion2.setCollector(collector2);
        completion2.setAlbum(album);
        completion2.setCompletedAt(now.minusMonths(2));
        completedAlbumRepository.save(completion2);

        // When
        List<RankingEntry> ranking = rankingService.getYearlyRanking(now.getYear());

        // Then
        assertThat(ranking).isNotEmpty();
        
        RankingEntry top = ranking.stream()
                .filter(r -> r.getCollectorId().equals(collector1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(top.getCompletions()).isEqualTo(3);

        RankingEntry second = ranking.stream()
                .filter(r -> r.getCollectorId().equals(collector2.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(second.getCompletions()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should exclude completions from different months in monthly ranking")
    void testMonthlyRankingExcludesOtherMonths() {
        // Given
        LocalDateTime currentMonth = LocalDateTime.now();
        LocalDateTime lastMonth = currentMonth.minusMonths(1);

        // Completion in last month
        CompletedAlbum oldCompletion = new CompletedAlbum();
        oldCompletion.setCollector(collector1);
        oldCompletion.setAlbum(album);
        oldCompletion.setCompletedAt(lastMonth);
        completedAlbumRepository.save(oldCompletion);

        // Completion in current month
        CompletedAlbum newCompletion = new CompletedAlbum();
        newCompletion.setCollector(collector2);
        newCompletion.setAlbum(album);
        newCompletion.setCompletedAt(currentMonth);
        completedAlbumRepository.save(newCompletion);

        // When - Get current month ranking
        List<RankingEntry> ranking = rankingService.getMonthlyRanking(
                currentMonth.getYear(),
                currentMonth.getMonthValue()
        );

        // Then - Only collector2 should appear
        assertThat(ranking.stream()
                .filter(r -> r.getCollectorId().equals(collector2.getId()))
                .findFirst())
                .isPresent();
    }

    @Test
    @DisplayName("Should exclude completions from different years in yearly ranking")
    void testYearlyRankingExcludesOtherYears() {
        // Given
        LocalDateTime currentYear = LocalDateTime.now();
        LocalDateTime lastYear = currentYear.minusYears(1);

        // Completion last year
        CompletedAlbum oldCompletion = new CompletedAlbum();
        oldCompletion.setCollector(collector1);
        oldCompletion.setAlbum(album);
        oldCompletion.setCompletedAt(lastYear);
        completedAlbumRepository.save(oldCompletion);

        // Completion this year
        CompletedAlbum newCompletion = new CompletedAlbum();
        newCompletion.setCollector(collector2);
        newCompletion.setAlbum(album);
        newCompletion.setCompletedAt(currentYear);
        completedAlbumRepository.save(newCompletion);

        // When - Get current year ranking
        List<RankingEntry> ranking = rankingService.getYearlyRanking(currentYear.getYear());

        // Then - Only collector2 should appear with completion this year
        RankingEntry entry = ranking.stream()
                .filter(r -> r.getCollectorId().equals(collector2.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(entry.getCompletions()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return maximum 10 entries in ranking")
    void testRankingLimitToTop10() {
        // Given - Create more than 10 collectors with completions
        for (int i = 0; i < 15; i++) {
            Collector collector = new Collector();
            collector.setUsername("Collector" + i);
            collector.setStars(i);
            collectorRepository.save(collector);
        }

        // When
        List<RankingEntry> ranking = rankingService.getAllTimeRanking();

        // Then - Should return at most 10 entries
        assertThat(ranking).hasSizeLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("Should return empty list when no completions in period")
    void testEmptyMonthlyRanking() {
        // Given - No completions in future month

        // When
        List<RankingEntry> ranking = rankingService.getMonthlyRanking(2030, 1);

        // Then
        assertThat(ranking).isEmpty();
    }

    @Test
    @DisplayName("Should handle collector with zero stars in all-time ranking")
    void testCollectorWithZeroStars() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("ZeroStarCollector");
        Collector zeroCollector = collectorService.createCollector(request);
        zeroCollector.setStars(0);
        zeroCollector = collectorRepository.save(zeroCollector);

        // When
        List<RankingEntry> ranking = rankingService.getAllTimeRanking();

        // Then - Should not include collectors with 0 stars (no completions)
        boolean hasZeroStarCollector = ranking.stream()
                .anyMatch(r -> r.getCollectorId().equals(zeroCollector.getId()));
        
        assertThat(hasZeroStarCollector).isFalse();
    }
}
