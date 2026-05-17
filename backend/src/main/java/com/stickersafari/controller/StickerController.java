package com.stickersafari.controller;

import com.stickersafari.dto.StickerRequest;
import com.stickersafari.dto.StickerResponse;
import com.stickersafari.entity.Sticker;
import com.stickersafari.mapper.StickerMapper;
import com.stickersafari.service.StickerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("stickers")
@RequiredArgsConstructor
@Slf4j
public class StickerController {

    private final StickerService stickerService;
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

    // Admin endpoint for pagination and search
    @GetMapping("/admin/paginated")
    public ResponseEntity<Map<String, Object>> getStickersWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long albumId) {
        
        log.debug("Getting stickers with pagination - page: {}, size: {}, sort: {} {}, search: {}, albumId: {}", 
                  page, size, sortBy, sortDir, search, albumId);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Sticker> stickerPage;
        
        if (search != null && !search.trim().isEmpty()) {
            stickerPage = stickerService.searchStickers(search, pageable);
        } else if (albumId != null) {
            if (albumId == -1) {
                // -1 means filter for stickers without albums
                stickerPage = stickerService.getStickersWithoutAlbumWithPagination(pageable);
            } else {
                stickerPage = stickerService.getStickersOfAlbumWithPagination(albumId, pageable);
            }
        } else {
            stickerPage = stickerService.getAllStickersWithPagination(pageable);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("stickers", stickerMapper.toResponseList(stickerPage.getContent()));
        response.put("currentPage", stickerPage.getNumber());
        response.put("totalItems", stickerPage.getTotalElements());
        response.put("totalPages", stickerPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/open/{albumId}/collector/{userId}")
    public ResponseEntity<List<StickerResponse>> openPack(@PathVariable("albumId") Long albumId, 
                                           @PathVariable("userId") Long userId,
                                           @RequestParam(value = "size", defaultValue = "3") int size) {
        log.info("Opening pack for user {} from album {}, size: {}", userId, albumId, size);
        
        List<Sticker> randomStickers = stickerService.openPackForCollector(albumId, userId, size);
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

    @PutMapping("/{id}/remove-from-album")
    public ResponseEntity<StickerResponse> removeStickerFromAlbum(@PathVariable("id") Long id) {
        log.info("Removing sticker {} from album", id);
        Sticker sticker = stickerService.removeStickerFromAlbum(id);
        return ResponseEntity.ok(stickerMapper.toResponse(sticker));
    }
}
