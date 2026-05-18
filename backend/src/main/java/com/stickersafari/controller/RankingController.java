package com.stickersafari.controller;

import com.stickersafari.dto.PagedRankingResponse;
import com.stickersafari.dto.RankingFilterRequest;
import com.stickersafari.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
@Slf4j
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<PagedRankingResponse> getMonthlyRanking(
            @PathVariable int year,
            @PathVariable int month,
            RankingFilterRequest filter) {
        
        log.debug("Monthly ranking request: {}-{}, page={}, size={}, search={}", 
                  year, month, filter.getPage(), filter.getSize(), filter.getSearch());
        PagedRankingResponse response = rankingService.getRankingByType(
            "monthly", year, month, filter.getPage(), filter.getSize(), filter.getSearch());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<PagedRankingResponse> getYearlyRanking(
            @PathVariable int year,
            RankingFilterRequest filter) {
        
        log.debug("Yearly ranking request: {}, page={}, size={}, search={}", 
                  year, filter.getPage(), filter.getSize(), filter.getSearch());
        PagedRankingResponse response = rankingService.getRankingByType(
            "yearly", year, null, filter.getPage(), filter.getSize(), filter.getSearch());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-time")
    public ResponseEntity<PagedRankingResponse> getAllTimeRanking(RankingFilterRequest filter) {
        
        log.debug("All-time ranking request: page={}, size={}, search={}", 
                  filter.getPage(), filter.getSize(), filter.getSearch());
        PagedRankingResponse response = rankingService.getRankingByType(
            "all-time", null, null, filter.getPage(), filter.getSize(), filter.getSearch());
        return ResponseEntity.ok(response);
    }
}
