package com.stickersafari.service;

import com.stickersafari.dto.RankingEntry;

import java.util.List;

public interface RankingService {
    List<RankingEntry> getMonthlyRanking(int year, int month);
    List<RankingEntry> getYearlyRanking(int year);
    List<RankingEntry> getAllTimeRanking();
}
