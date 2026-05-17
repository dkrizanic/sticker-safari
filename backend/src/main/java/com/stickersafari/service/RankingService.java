package com.stickersafari.service;

import com.stickersafari.dto.PagedRankingResponse;
import com.stickersafari.dto.RankingEntry;

import java.util.List;

public interface RankingService {
    PagedRankingResponse getMonthlyRanking(int year, int month, int page, int size, String search);
    PagedRankingResponse getYearlyRanking(int year, int page, int size, String search);
    PagedRankingResponse getAllTimeRanking(int page, int size, String search);
    
    // Legacy methods for backwards compatibility (deprecated)
    @Deprecated
    List<RankingEntry> getMonthlyRanking(int year, int month);
    @Deprecated
    List<RankingEntry> getYearlyRanking(int year);
    @Deprecated
    List<RankingEntry> getAllTimeRanking();
}
