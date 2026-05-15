package com.stickersafari.service.impl;

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
    public List<RankingEntry> getYearlyRanking(int year) {
        log.info("Getting yearly ranking for {}", year);
        
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        
        List<Object[]> results = completedAlbumRepository.findTopCollectorsByPeriod(
            startDate, endDate, PageRequest.of(0, 10));
        
        return mapToRankingEntries(results);
    }

    @Override
    public List<RankingEntry> getAllTimeRanking() {
        log.info("Getting all-time ranking");
        
        List<Object[]> results = completedAlbumRepository.findTopCollectorsAllTime(
            PageRequest.of(0, 10));
        
        return mapToRankingEntries(results);
    }

    private List<RankingEntry> mapToRankingEntries(List<Object[]> results) {
        List<RankingEntry> rankings = new ArrayList<>();
        for (Object[] result : results) {
            Long collectorId = ((Number) result[0]).longValue();
            String username = (String) result[1];
            Long count = ((Number) result[2]).longValue();
            rankings.add(new RankingEntry(collectorId, username, count));
        }
        return rankings;
    }
}
