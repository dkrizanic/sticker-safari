package com.stickersafari.service.impl;

import com.stickersafari.dto.StickerRequest;
import com.stickersafari.entity.Album;
import com.stickersafari.entity.Sticker;
import com.stickersafari.exception.EntityNotFoundException;
import com.stickersafari.repository.AlbumRepository;
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
    private final AlbumRepository albumRepository;

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

    @Override
    public List<Sticker> getAllStickers() {
        log.debug("Getting all stickers");
        return stickerRepository.findAll();
    }

    @Override
    @Transactional
    public Sticker createSticker(StickerRequest request) {
        log.info("Creating new sticker: {}", request.getName());
        Sticker sticker = new Sticker();
        sticker.setName(request.getName());
        sticker.setLatinName(request.getLatinName());
        sticker.setHabitat(request.getHabitat());
        sticker.setDescription(request.getDescription());
        sticker.setSrc(request.getSrc());
        
        // Set album if provided
        if (request.getAlbumId() != null) {
            Album album = albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new EntityNotFoundException("Album not found with ID: " + request.getAlbumId()));
            sticker.setAlbum(album);
        }
        
        return stickerRepository.save(sticker);
    }

    @Override
    @Transactional
    public Sticker updateSticker(Long stickerId, StickerRequest request) {
        log.info("Updating sticker with ID: {}", stickerId);
        Sticker sticker = findOne(stickerId);
        
        sticker.setName(request.getName());
        sticker.setLatinName(request.getLatinName());
        sticker.setHabitat(request.getHabitat());
        sticker.setDescription(request.getDescription());
        sticker.setSrc(request.getSrc());
        
        // Cannot change album via update (as per requirements)
        
        return stickerRepository.save(sticker);
    }
}
