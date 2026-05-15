package com.stickersafari.controller;

import com.stickersafari.dto.StickerRequest;
import com.stickersafari.dto.StickerResponse;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.exception.DailyLimitExceededException;
import com.stickersafari.mapper.StickerMapper;
import com.stickersafari.service.CollectorService;
import com.stickersafari.service.StickerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("stickers")
@RequiredArgsConstructor
@Slf4j
public class StickerController {

    private final StickerService stickerService;
    private final CollectorService collectorService;
    private final StickerMapper stickerMapper;

    @GetMapping
    public ResponseEntity<List<StickerResponse>> getAllStickers() {
        log.debug("Getting all stickers");
        List<Sticker> stickers = stickerService.getAllStickers();
        return ResponseEntity.ok(stickerMapper.toResponseList(stickers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StickerResponse> getSticker(@PathVariable("id") Long id) {
        log.debug("Getting sticker with ID: {}", id);
        Sticker sticker = stickerService.findOne(id);
        return ResponseEntity.ok(stickerMapper.toResponse(sticker));
    }

    @GetMapping("/open/{albumId}/collector/{userId}")
    public ResponseEntity<List<StickerResponse>> openPack(@PathVariable("albumId") Long albumId, 
                                           @PathVariable("userId") Long userId,
                                           @RequestParam(value = "size", defaultValue = "3") int size) {
        log.info("Opening pack for user {} from album {}, size: {}", userId, albumId, size);
        
        // Check daily limit
        Collector collector = collectorService.findById(userId);
        LocalDate today = LocalDate.now();
        
        // Reset counter if it's a new day
        if (collector.getLastPackOpenDate() == null || !collector.getLastPackOpenDate().equals(today)) {
            collector.setLastPackOpenDate(today);
            collector.setDailyStickerCount(0);
        }
        
        // Check if opening this pack would exceed daily limit
        int currentCount = collector.getDailyStickerCount() != null ? collector.getDailyStickerCount() : 0;
        if (currentCount + size > 20) {
            int remaining = 20 - currentCount;
            throw new DailyLimitExceededException(
                String.format("Daily limit exceeded. You can only open %d more sticker(s) today. Limit resets at midnight.", remaining)
            );
        }
        
        // Open pack
        List<Sticker> randomStickers = stickerService.openPack(albumId, userId, size);
        List<Long> stickerIds = randomStickers.stream()
                .map(Sticker::getId)
                .collect(Collectors.toList());
        
        collectorService.addStickersToCollector(userId, stickerIds);
        
        // Update daily counter
        collector.setDailyStickerCount(currentCount + size);
        collectorService.save(collector);
        
        return ResponseEntity.ok(stickerMapper.toResponseList(randomStickers));
    }

    @PostMapping
    public ResponseEntity<StickerResponse> createSticker(@RequestBody StickerRequest request) {
        log.info("Creating sticker: {}", request.getName());
        Sticker sticker = stickerService.createSticker(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(stickerMapper.toResponse(sticker));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StickerResponse> updateSticker(@PathVariable("id") Long id,
                                                          @RequestBody StickerRequest request) {
        log.info("Updating sticker with ID: {}", id);
        Sticker sticker = stickerService.updateSticker(id, request);
        return ResponseEntity.ok(stickerMapper.toResponse(sticker));
    }
}
