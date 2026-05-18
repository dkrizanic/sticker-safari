package com.stickersafari.service;

import com.stickersafari.dto.StickerRequest;
import com.stickersafari.entity.Sticker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface StickerService {

    Sticker findOne(Long stickerId);

    List<Sticker> getStickersOfAlbum(Long albumId);
    
    List<Sticker> openPack(Long albumId, Long userId, int size);
    
    /**
     * Opens a pack for a collector with daily limit validation and album completion checks
     */
    List<Sticker> openPackForCollector(Long albumId, Long collectorId, int size);
    
    // CRUD operations
    List<Sticker> getAllStickers();
    Page<Sticker> getAllStickersWithPagination(Pageable pageable);
    Page<Sticker> searchStickers(String search, Pageable pageable);
    Page<Sticker> getStickersOfAlbumWithPagination(Long albumId, Pageable pageable);
    Page<Sticker> getStickersWithoutAlbumWithPagination(Pageable pageable);
    Sticker createSticker(StickerRequest request);
    Sticker updateSticker(Long stickerId, StickerRequest request);
    Sticker removeStickerFromAlbum(Long stickerId);
    
    // Filtered pagination
    Map<String, Object> getFilteredStickers(int page, int size, String sortBy, String sortDir, String search, Long albumId);
}
