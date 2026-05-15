package com.stickersafari.service.impl;

import com.stickersafari.dto.AlbumCompletionResponse;
import com.stickersafari.entity.Album;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.exception.EntityNotFoundException;
import com.stickersafari.mapper.StickerMapper;
import com.stickersafari.repository.AlbumRepository;
import com.stickersafari.service.AlbumService;
import com.stickersafari.service.CollectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public AlbumServiceImpl(@Lazy CollectorService collectorService, AlbumRepository albumRepository, StickerMapper stickerMapper) {
        this.collectorService = collectorService;
        this.albumRepository = albumRepository;
        this.stickerMapper = stickerMapper;
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
}
