package com.stickersafari.controller;

import com.stickersafari.dto.StickerResponse;
import com.stickersafari.entity.Sticker;
import com.stickersafari.mapper.StickerMapper;
import com.stickersafari.service.CollectorService;
import com.stickersafari.service.StickerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/open/{albumId}/collector/{userId}")
    public ResponseEntity<List<StickerResponse>> openPack(@PathVariable("albumId") Long albumId, 
                                           @PathVariable("userId") Long userId,
                                           @RequestParam(value = "size", defaultValue = "3") int size) {
        log.info("Opening pack for user {} from album {}, size: {}", userId, albumId, size);
        
        List<Sticker> randomStickers = stickerService.openPack(albumId, userId, size);
        List<Long> stickerIds = randomStickers.stream()
                .map(Sticker::getId)
                .collect(Collectors.toList());
        
        collectorService.addStickersToCollector(userId, stickerIds);
        
        return ResponseEntity.ok(stickerMapper.toResponseList(randomStickers));
    }
}
