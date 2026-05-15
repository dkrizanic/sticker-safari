package com.stickersafari.service.impl;

import com.stickersafari.dto.AlbumCompletionResponse;
import com.stickersafari.dto.AlbumRequest;
import com.stickersafari.entity.Album;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.exception.EntityNotFoundException;
import com.stickersafari.mapper.StickerMapper;
import com.stickersafari.repository.AlbumRepository;
import com.stickersafari.repository.StickerRepository;
import com.stickersafari.service.AlbumService;
import com.stickersafari.service.CollectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class AlbumServiceImpl implements AlbumService {

    private final CollectorService collectorService;
    private final AlbumRepository albumRepository;
    private final StickerMapper stickerMapper;
    private final StickerRepository stickerRepository;

    public AlbumServiceImpl(@Lazy CollectorService collectorService, AlbumRepository albumRepository, 
                            StickerMapper stickerMapper, StickerRepository stickerRepository) {
        this.collectorService = collectorService;
        this.albumRepository = albumRepository;
        this.stickerMapper = stickerMapper;
        this.stickerRepository = stickerRepository;
    }

    @Override
    public List<Sticker> getMissingStickers(Album album, Long collectorId) {
        log.debug("Getting missing stickers for album {} and collector {}", album.getId(), collectorId);
        Collector collector = collectorService.findById(collectorId);
        return getUnownedStickersOfAlbum(album.getStickers(), collector.getOwnedStickers());
    }

    @Override
    public Album getAlbum(Long albumId) {
        log.debug("Finding album with ID: {}", albumId);
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Album not found with ID: " + albumId));
    }

    @Override
    public AlbumCompletionResponse getAlbumCompletion(Long albumId, Long collectorId) {
        log.debug("Calculating album completion for album {} and collector {}", albumId, collectorId);
        Album album = getAlbum(albumId);
        List<Sticker> missingStickers = getMissingStickers(album, collectorId);
        
        float completion = 100 - ((float) missingStickers.size() / album.getStickers().size() * 100);
        
        AlbumCompletionResponse response = new AlbumCompletionResponse();
        response.setCompletion(completion);
        response.setMissingStickerList(stickerMapper.toResponseList(missingStickers));
        
        return response;
    }

    private List<Sticker> getUnownedStickersOfAlbum(List<Sticker> albumStickers, Map<Long, Integer> ownedStickers) {
        return albumStickers.stream()
                .filter((sticker) -> ownedStickers.containsKey(sticker.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Album> getAllAlbums() {
        log.debug("Getting all albums");
        return albumRepository.findAll();
    }

    @Override
    @Transactional
    public Album createAlbum(AlbumRequest request) {
        log.info("Creating new album: {}", request.getName());
        Album album = new Album();
        album.setName(request.getName());
        album.setStickers(new ArrayList<>());
        
        // Add existing stickers without album if provided
        if (request.getStickerIds() != null && !request.getStickerIds().isEmpty()) {
            List<Sticker> stickers = stickerRepository.findAllById(request.getStickerIds());
            for (Sticker sticker : stickers) {
                if (sticker.getAlbum() == null) {
                    sticker.setAlbum(album);
                    album.getStickers().add(sticker);
                }
            }
        }
        
        return albumRepository.save(album);
    }

    @Override
    @Transactional
    public Album updateAlbum(Long albumId, AlbumRequest request) {
        log.info("Updating album with ID: {}", albumId);
        Album album = getAlbum(albumId);
        album.setName(request.getName());
        
        // Update stickers: remove stickers not in the new list (but don't delete them)
        if (request.getStickerIds() != null) {
            // Remove album reference from stickers not in the new list
            List<Sticker> currentStickers = new ArrayList<>(album.getStickers());
            for (Sticker sticker : currentStickers) {
                if (!request.getStickerIds().contains(sticker.getId())) {
                    sticker.setAlbum(null);
                    album.getStickers().remove(sticker);
                }
            }
            
            // Add new stickers
            for (Long stickerId : request.getStickerIds()) {
                if (album.getStickers().stream().noneMatch(s -> s.getId().equals(stickerId))) {
                    Sticker sticker = stickerRepository.findById(stickerId)
                            .orElseThrow(() -> new EntityNotFoundException("Sticker not found with ID: " + stickerId));
                    if (sticker.getAlbum() == null) {
                        sticker.setAlbum(album);
                        album.getStickers().add(sticker);
                    }
                }
            }
        }
        
        return albumRepository.save(album);
    }

    @Override
    @Transactional
    public void deleteAlbum(Long albumId) {
        log.info("Deleting album with ID: {}", albumId);
        Album album = getAlbum(albumId);
        
        // Remove album reference from all stickers (but don't delete them)
        for (Sticker sticker : album.getStickers()) {
            sticker.setAlbum(null);
        }
        
        albumRepository.delete(album);
    }
}
