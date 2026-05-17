package com.stickersafari.service.impl;

import com.stickersafari.dto.StickerRequest;
import com.stickersafari.entity.Album;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Notification;
import com.stickersafari.entity.Sticker;
import com.stickersafari.exception.DailyLimitExceededException;
import com.stickersafari.exception.EntityNotFoundException;
import com.stickersafari.repository.AlbumRepository;
import com.stickersafari.repository.StickerRepository;
import com.stickersafari.service.AlbumService;
import com.stickersafari.service.CollectorService;
import com.stickersafari.service.NotificationService;
import com.stickersafari.service.StickerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class StickerServiceImpl implements StickerService {

    private final StickerRepository stickerRepository;
    private final AlbumRepository albumRepository;
    private final CollectorService collectorService;
    private final AlbumService albumService;
    private final NotificationService notificationService;

    public StickerServiceImpl(StickerRepository stickerRepository, 
                              AlbumRepository albumRepository,
                              @Lazy CollectorService collectorService,
                              AlbumService albumService,
                              NotificationService notificationService) {
        this.stickerRepository = stickerRepository;
        this.albumRepository = albumRepository;
        this.collectorService = collectorService;
        this.albumService = albumService;
        this.notificationService = notificationService;
    }

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
    @Transactional
    public List<Sticker> openPackForCollector(Long albumId, Long collectorId, int size) {
        log.info("Opening pack for collector {} from album {}, size: {}", collectorId, albumId, size);
        
        // Validate and check daily limit
        Collector collector = collectorService.findById(collectorId);
        validateDailyLimit(collector, size);
        
        // Open pack
        List<Sticker> randomStickers = openPack(albumId, collectorId, size);
        
        // Add stickers to collector
        List<Long> stickerIds = randomStickers.stream()
                .map(Sticker::getId)
                .collect(Collectors.toList());
        collectorService.addStickersToCollector(collectorId, stickerIds);
        
        // Update daily counter
        updateDailyCounter(collector, size);
        
        // Check for completable albums and send notifications
        checkAndNotifyCompletableAlbums(collector, randomStickers);
        
        return randomStickers;
    }

    private void validateDailyLimit(Collector collector, int size) {
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
    }

    private void updateDailyCounter(Collector collector, int size) {
        int currentCount = collector.getDailyStickerCount() != null ? collector.getDailyStickerCount() : 0;
        collector.setDailyStickerCount(currentCount + size);
        collectorService.save(collector);
    }

    private void checkAndNotifyCompletableAlbums(Collector collector, List<Sticker> newStickers) {
        // Get unique albums from the newly opened stickers
        Set<Long> albumIds = new HashSet<>();
        for (Sticker sticker : newStickers) {
            if (sticker.getAlbum() != null) {
                albumIds.add(sticker.getAlbum().getId());
            }
        }
        
        // Check each album if it can be completed
        for (Long albumId : albumIds) {
            if (albumService.canCompleteAlbum(albumId, collector.getId())) {
                Album album = albumService.getAlbum(albumId);
                String message = String.format("Great news! You have collected all stickers for album '%s'. You can now complete it to earn a star! ⭐", album.getName());
                
                // Check if this notification already exists and is unread
                if (notificationService.notificationExists(collector.getId(), message)) {
                    log.debug("Notification already exists for collector {} and album {}, skipping", collector.getId(), albumId);
                    continue;
                }
                
                Notification notification = new Notification();
                notification.setRecipient(collector);
                notification.setMessage(message);
                notification.setCreatedAt(LocalDateTime.now());
                notification.setRead(false);
                notificationService.saveNotification(notification);
                log.info("Collector {} can complete album {}", collector.getId(), albumId);
            }
        }
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

    @Override
    public Page<Sticker> getAllStickersWithPagination(Pageable pageable) {
        log.debug("Getting all stickers with pagination: page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());
        return stickerRepository.findAll(pageable);
    }

    @Override
    public Page<Sticker> searchStickers(String search, Pageable pageable) {
        log.debug("Searching stickers with query '{}' and pagination: page {}, size {}", 
                  search, pageable.getPageNumber(), pageable.getPageSize());
        if (search == null || search.trim().isEmpty()) {
            return getAllStickersWithPagination(pageable);
        }
        return stickerRepository.searchStickers(search.trim(), pageable);
    }

    @Override
    public Page<Sticker> getStickersOfAlbumWithPagination(Long albumId, Pageable pageable) {
        log.debug("Getting stickers for album ID {} with pagination: page {}, size {}", 
                  albumId, pageable.getPageNumber(), pageable.getPageSize());
        return stickerRepository.findByAlbumIdWithPagination(albumId, pageable);
    }
    
    @Override
    public Page<Sticker> getStickersWithoutAlbumWithPagination(Pageable pageable) {
        log.debug("Getting stickers without album with pagination: page {}, size {}", 
                  pageable.getPageNumber(), pageable.getPageSize());
        return stickerRepository.findByAlbumIsNullWithPagination(pageable);
    }
}
