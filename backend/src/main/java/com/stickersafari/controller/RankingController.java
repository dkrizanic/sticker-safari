package com.stickersafari.controller;

import com.stickersafari.dto.PagedRankingResponse;
import com.stickersafari.dto.RankingEntry;
import com.stickersafari.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        log.info("Getting monthly ranking for {}-{} (page={}, size={}, search={})", year, month, page, size, search);
        PagedRankingResponse rankings = rankingService.getMonthlyRanking(year, month, page, size, search);
        return ResponseEntity.ok(rankings);
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<PagedRankingResponse> getYearlyRanking(
            @PathVariable int year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        log.info("Getting yearly ranking for {} (page={}, size={}, search={})", year, page, size, search);
        PagedRankingResponse rankings = rankingService.getYearlyRanking(year, page, size, search);
        return ResponseEntity.ok(rankings);
    }

    @GetMapping("/all-time")
    public ResponseEntity<PagedRankingResponse> getAllTimeRanking(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        log.info("Getting all-time ranking (page={}, size={}, search={})", page, size, search);
        PagedRankingResponse rankings = rankingService.getAllTimeRanking(page, size, search);
        return ResponseEntity.ok(rankings);
    }
}
