package com.stickersafari.controller;

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
    public ResponseEntity<List<RankingEntry>> getMonthlyRanking(@PathVariable int year,
                                                                  @PathVariable int month) {
        log.info("Getting monthly ranking for {}-{}", year, month);
        List<RankingEntry> rankings = rankingService.getMonthlyRanking(year, month);
        return ResponseEntity.ok(rankings);
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<List<RankingEntry>> getYearlyRanking(@PathVariable int year) {
        log.info("Getting yearly ranking for {}", year);
        List<RankingEntry> rankings = rankingService.getYearlyRanking(year);
        return ResponseEntity.ok(rankings);
    }

    @GetMapping("/all-time")
    public ResponseEntity<List<RankingEntry>> getAllTimeRanking() {
        log.info("Getting all-time ranking");
        List<RankingEntry> rankings = rankingService.getAllTimeRanking();
        return ResponseEntity.ok(rankings);
    }
}
