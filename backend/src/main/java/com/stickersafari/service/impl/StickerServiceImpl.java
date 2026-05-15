package com.stickersafari.service.impl;

import com.stickersafari.entity.Sticker;
import com.stickersafari.exception.EntityNotFoundException;
import com.stickersafari.repository.StickerRepository;
import com.stickersafari.service.StickerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StickerServiceImpl implements StickerService {

    private final StickerRepository stickerRepository;

    @Override
    public Sticker findOne(Long stickerId) {
        log.debug("Finding sticker with ID: {}", stickerId);
        return stickerRepository.findById(stickerId)
                .orElseThrow(() -> new EntityNotFoundException("Sticker not found with ID: " + stickerId));
    }

    @Override
    public List<Sticker> getStickersOfAlbum(Long albumId) {
        log.debug("Getting stickers for album ID: {}", albumId);
        return stickerRepository.findByAlbumId(albumId);
    }
    
    @Override
    @Transactional
    public List<Sticker> openPack(Long albumId, Long userId, int size) {
        log.info("Opening pack for user {} from album {}, size: {}", userId, albumId, size);
        
        if (size < 1 || size > 10) {
            throw new IllegalArgumentException("Pack size must be between 1 and 10");
        }
        
        List<Sticker> albumStickers = getStickersOfAlbum(albumId);
        if (albumStickers.isEmpty()) {
            throw new EntityNotFoundException("No stickers found in album: " + albumId);
        }
        
        List<Sticker> randomStickers = new java.util.ArrayList<>();
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < size; i++) {
            int randomIndex = random.nextInt(albumStickers.size());
            Sticker randomSticker = albumStickers.get(randomIndex);
            randomStickers.add(randomSticker);
        }
        
        return randomStickers;
    }
}
