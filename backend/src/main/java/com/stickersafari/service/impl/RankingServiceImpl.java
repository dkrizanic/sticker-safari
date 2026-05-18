package com.stickersafari.service.impl;

import com.stickersafari.dto.PagedRankingResponse;
import com.stickersafari.dto.RankingEntry;
import com.stickersafari.repository.CompletedAlbumRepository;
import com.stickersafari.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RankingServiceImpl implements RankingService {

    private final CompletedAlbumRepository completedAlbumRepository;

    @Override
    public PagedRankingResponse getMonthlyRanking(int year, int month, int page, int size, String search) {
        log.info("Getting monthly ranking for {}-{} with pagination (page={}, size={}, search={})", year, month, page, size, search);
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        
        String searchTerm = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        
        List<Object[]> results = completedAlbumRepository.findTopCollectorsByPeriodWithSearch(
            startDate, endDate, searchTerm, PageRequest.of(page, size));
        
        long totalItems = completedAlbumRepository.countCollectorsByPeriodWithSearch(startDate, endDate, searchTerm);
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        List<RankingEntry> rankings = mapToRankingEntries(results);
        return new PagedRankingResponse(rankings, page, totalPages, totalItems, size);
    }

    @Override
    public PagedRankingResponse getYearlyRanking(int year, int page, int size, String search) {
        log.info("Getting yearly ranking for {} with pagination (page={}, size={}, search={})", year, page, size, search);
        
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        
        String searchTerm = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        
        List<Object[]> results = completedAlbumRepository.findTopCollectorsByPeriodWithSearch(
            startDate, endDate, searchTerm, PageRequest.of(page, size));
        
        long totalItems = completedAlbumRepository.countCollectorsByPeriodWithSearch(startDate, endDate, searchTerm);
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        List<RankingEntry> rankings = mapToRankingEntries(results);
        return new PagedRankingResponse(rankings, page, totalPages, totalItems, size);
    }

    @Override
    public PagedRankingResponse getAllTimeRanking(int page, int size, String search) {
        log.info("Getting all-time ranking with pagination (page={}, size={}, search={})", page, size, search);
        
        String searchTerm = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        
        List<Object[]> results = completedAlbumRepository.findTopCollectorsAllTimeWithSearch(
            searchTerm, PageRequest.of(page, size));
        
        long totalItems = completedAlbumRepository.countCollectorsAllTimeWithSearch(searchTerm);
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        List<RankingEntry> rankings = mapToAllTimeRankingEntries(results);
        return new PagedRankingResponse(rankings, page, totalPages, totalItems, size);
    }

    @Override
    public PagedRankingResponse getRankingByType(String type, Integer year, Integer month, int page, int size, String search) {
        log.debug("Getting ranking by type: {} (year={}, month={}, page={}, size={}, search={})", 
                  type, year, month, page, size, search);
        
        return switch (type.toLowerCase()) {
            case "monthly" -> {
                if (year == null || month == null) {
                    throw new IllegalArgumentException("Year and month are required for monthly ranking");
                }
                yield getMonthlyRanking(year, month, page, size, search);
            }
            case "yearly" -> {
                if (year == null) {
                    throw new IllegalArgumentException("Year is required for yearly ranking");
                }
                yield getYearlyRanking(year, page, size, search);
            }
            case "all-time" -> getAllTimeRanking(page, size, search);
            default -> throw new IllegalArgumentException("Invalid ranking type: " + type);
        };
    }

    // Legacy methods for backwards compatibility
    @Override
    @Deprecated
    public List<RankingEntry> getMonthlyRanking(int year, int month) {
        log.info("Getting monthly ranking for {}-{}", year, month);
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        
        List<Object[]> results = completedAlbumRepository.findTopCollectorsByPeriod(
            startDate, endDate, PageRequest.of(0, 10));
        
        return mapToRankingEntries(results);
    }

    @Override
    @Deprecated
    public List<RankingEntry> getYearlyRanking(int year) {
        log.info("Getting yearly ranking for {}", year);
        
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        
        List<Object[]> results = completedAlbumRepository.findTopCollectorsByPeriod(
            startDate, endDate, PageRequest.of(0, 10));
        
        return mapToRankingEntries(results);
    }

    @Override
    @Deprecated
    public List<RankingEntry> getAllTimeRanking() {
        log.info("Getting all-time ranking");
        
        List<Object[]> results = completedAlbumRepository.findTopCollectorsAllTime(
            PageRequest.of(0, 10));
        
        return mapToAllTimeRankingEntries(results);
    }

    private List<RankingEntry> mapToRankingEntries(List<Object[]> results) {
        List<RankingEntry> rankings = new ArrayList<>();
        for (Object[] result : results) {
            Long collectorId = ((Number) result[0]).longValue();
            String username = (String) result[1];
            Long completions = ((Number) result[2]).longValue();
            rankings.add(new RankingEntry(collectorId, username, completions, 0L, completions));
        }
        return rankings;
    }

    private List<RankingEntry> mapToAllTimeRankingEntries(List<Object[]> results) {
        List<RankingEntry> rankings = new ArrayList<>();
        for (Object[] result : results) {
            Long collectorId = ((Number) result[0]).longValue();
            String username = (String) result[1];
            Long stars = result[2] != null ? ((Number) result[2]).longValue() : 0L;
            Long completions = ((Number) result[3]).longValue();
            Long score = ((Number) result[4]).longValue();
            rankings.add(new RankingEntry(collectorId, username, completions, stars, score));
        }
        return rankings;
    }
}
