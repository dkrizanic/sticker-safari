package com.stickersafari.service;

import com.stickersafari.dto.StickerRequest;
import com.stickersafari.entity.Sticker;

import java.util.List;

public interface StickerService {

    Sticker findOne(Long stickerId);

    List<Sticker> getStickersOfAlbum(Long albumId);
    
    List<Sticker> openPack(Long albumId, Long userId, int size);
    
    // CRUD operations
    List<Sticker> getAllStickers();
    Sticker createSticker(StickerRequest request);
    Sticker updateSticker(Long stickerId, StickerRequest request);
}
