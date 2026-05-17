package com.stickersafari.service.impl;

import com.stickersafari.dto.AlbumCompletionResponse;
import com.stickersafari.dto.AlbumRequest;
import com.stickersafari.entity.Album;
import com.stickersafari.entity.AlbumStatus;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.CompletedAlbum;
import com.stickersafari.entity.Notification;
import com.stickersafari.entity.Sticker;
import com.stickersafari.exception.EntityNotFoundException;
import com.stickersafari.mapper.StickerMapper;
import com.stickersafari.repository.AlbumRepository;
import com.stickersafari.repository.CompletedAlbumRepository;
import com.stickersafari.repository.StickerRepository;
import com.stickersafari.service.AlbumService;
import com.stickersafari.service.CollectorService;
import com.stickersafari.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final CompletedAlbumRepository completedAlbumRepository;
    private final NotificationService notificationService;

    public AlbumServiceImpl(@Lazy CollectorService collectorService, AlbumRepository albumRepository, 
                            StickerMapper stickerMapper, StickerRepository stickerRepository,
                            CompletedAlbumRepository completedAlbumRepository,
                            @Lazy NotificationService notificationService) {
        this.collectorService = collectorService;
        this.albumRepository = albumRepository;
        this.stickerMapper = stickerMapper;
        this.stickerRepository = stickerRepository;
        this.completedAlbumRepository = completedAlbumRepository;
        this.notificationService = notificationService;
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
                .filter((sticker) -> !ownedStickers.containsKey(sticker.getId()))
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
        album.setStatus(AlbumStatus.PENDING); // Default status
        
        // Add existing stickers without album if provided
        if (request.getStickerIds() != null && !request.getStickerIds().isEmpty()) {
            List<Sticker> stickers = stickerRepository.findAllById(request.getStickerIds());
            for (Sticker sticker : stickers) {
                if (sticker.getAlbum() == null) {
                    sticker.setAlbum(album);
                    album.getStickers().add(sticker);
                }
            }
            
            // If album has at least one sticker, set status to PUBLISHED
            if (!album.getStickers().isEmpty()) {
                album.setStatus(AlbumStatus.PUBLISHED);
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
        
        // Update status: manual override takes priority, otherwise auto-determine
        if (request.getStatus() != null) {
            // Validate: cannot publish album without stickers
            if (request.getStatus() == AlbumStatus.PUBLISHED && album.getStickers().isEmpty()) {
                throw new IllegalArgumentException("Cannot publish album without stickers");
            }
            album.setStatus(request.getStatus());
            log.info("Album status manually set to: {}", request.getStatus());
        } else if (request.getStickerIds() != null) {
            // Auto-determine status based on sticker count only if stickers were updated
            if (album.getStickers().isEmpty()) {
                album.setStatus(AlbumStatus.PENDING);
            } else {
                album.setStatus(AlbumStatus.PUBLISHED);
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

    @Override
    public boolean canCompleteAlbum(Long albumId, Long collectorId) {
        log.debug("Checking if collector {} can complete album {}", collectorId, albumId);
        Album album = getAlbum(albumId);
        List<Sticker> missingStickers = getMissingStickers(album, collectorId);
        return missingStickers.isEmpty();
    }

    @Override
    @Transactional
    public void completeAlbum(Long albumId, Long collectorId) {
        log.info("Completing album {} for collector {}", albumId, collectorId);
        
        Album album = getAlbum(albumId);
        Collector collector = collectorService.findById(collectorId);
        
        // Check if collector has all stickers
        if (!canCompleteAlbum(albumId, collectorId)) {
            throw new IllegalStateException("Cannot complete album - missing stickers");
        }
        
        // Check if this collector has completed this album before
        boolean hasCompletedBefore = completedAlbumRepository.findAll().stream()
                .anyMatch(ca -> ca.getCollector().getId().equals(collectorId) && ca.getAlbum().getId().equals(albumId));
        
        // Count how many distinct collectors have completed this album before
        // (used to determine if this collector gets a star for being in top 3)
        long previousCompletions = completedAlbumRepository.countDistinctCollectorsByAlbum(albumId);
        
        // Remove one copy of each sticker from the album
        for (Sticker sticker : album.getStickers()) {
            try {
                collector.removeSticker(sticker.getId());
            } catch (Exception e) {
                throw new IllegalStateException("Error removing sticker: " + e.getMessage());
            }
        }
        
        // Award a star ONLY if this is the first time completing this album AND they're in top 3
        boolean earnedStar = false;
        if (!hasCompletedBefore && previousCompletions < 3) {
            collector.setStars(collector.getStars() != null ? collector.getStars() + 1 : 1);
            earnedStar = true;
        }
        collectorService.save(collector);
        
        // Record completion
        CompletedAlbum completedAlbum = new CompletedAlbum();
        completedAlbum.setCollector(collector);
        completedAlbum.setAlbum(album);
        completedAlbum.setCompletedAt(LocalDateTime.now());
        completedAlbumRepository.save(completedAlbum);
        
        // Send notification based on whether they earned a star
        Notification notification = new Notification();
        notification.setRecipient(collector);
        
        if (earnedStar) {
            long position = previousCompletions + 1;
            notification.setMessage(String.format("Congratulations! You're #%d to complete '%s' and earned a ⭐ STAR! (Worth 5 points)", position, album.getName()));
        } else if (hasCompletedBefore) {
            notification.setMessage(String.format("Congratulations! You completed '%s' again! Great job!", album.getName()));
        } else {
            notification.setMessage(String.format("Congratulations! You completed '%s'! (+1 point)", album.getName()));
        }
        
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notificationService.saveNotification(notification);
        
        log.info("Album {} completed by collector {}. Earned star: {}. Total stars: {}", albumId, collectorId, earnedStar, collector.getStars());
    }
}
