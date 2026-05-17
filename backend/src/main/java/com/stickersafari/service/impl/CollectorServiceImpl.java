package com.stickersafari.service.impl;

import com.stickersafari.dto.CollectorRequest;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Role;
import com.stickersafari.entity.Sticker;
import com.stickersafari.entity.User;
import com.stickersafari.exception.EntityNotFoundException;
import com.stickersafari.repository.CollectorRepository;
import com.stickersafari.service.AlbumService;
import com.stickersafari.service.CollectorService;
import com.stickersafari.service.StickerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = true)
public class CollectorServiceImpl implements CollectorService {

    private final CollectorRepository collectorRepository;
    private final AlbumService albumService;
    private final StickerService stickerService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Collector findById(Long id) {
        log.debug("Finding collector by ID: {}", id);
        return collectorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collector not found with ID: " + id));
    }

    @Override
    public Collector findByUsername(String username) {
        log.debug("Finding collector by username: {}", username);
        Collector collector = collectorRepository.findByUsername(username);
        if (collector == null) {
            throw new EntityNotFoundException("Collector not found with username: " + username);
        }
        return collector;
    }

    @Override
    @Transactional
    public Collector save(Collector collector) {
        log.info("Saving collector: {}", collector.getUsername());
        return collectorRepository.save(collector);
    }

    @Override
    public List<Collector> findPotentialTradingPartners(Long senderId, Long offeredStickerId, Long receivedStickerId) {
        log.debug("Finding potential trading partners for sender: {}, offer: {} and request: {}", senderId, offeredStickerId, receivedStickerId);
        
        Sticker offeredSticker = stickerService.findOne(offeredStickerId);
        List<Collector> allCollectors = collectorRepository.findAll();

        return allCollectors.stream()
                .filter(collector -> !collector.getId().equals(senderId)) // Exclude the sender
                .filter(collector -> {
                    List<Sticker> receiverMissingStickers = albumService.getMissingStickers(
                            offeredSticker.getAlbum(), collector.getId());

                    boolean hasReceivedStickerDuplicate = collector.getOwnedStickers()
                            .containsKey(receivedStickerId) 
                            && collector.getOwnedStickers().get(receivedStickerId) > 0;

                    boolean doesntHaveOfferedSticker = receiverMissingStickers.stream()
                            .anyMatch(missingSticker -> missingSticker.getId().equals(offeredStickerId));

                    return hasReceivedStickerDuplicate && doesntHaveOfferedSticker;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void addStickersToCollector(Long collectorId, List<Long> stickerIds) {
        log.info("Adding {} stickers to collector {}", stickerIds.size(), collectorId);
        Collector collector = findById(collectorId);
        
        for (Long stickerId : stickerIds) {
            collector.addSticker(stickerId);
        }
        
        save(collector);
    }

    @Override
    public List<Collector> getAllCollectors() {
        log.debug("Getting all collectors");
        return collectorRepository.findAll();
    }

    @Override
    @Transactional
    public Collector createCollector(CollectorRequest request) {
        log.info("Creating new collector: {}", request.getUsername());
        
        // Create User entity for authentication
        User user = new User();
        user.setUsername(request.getUsername());
        // Hash the default password using BCrypt
        user.setPassword(passwordEncoder.encode("collector"));
        user.setRole(Role.COLLECTOR);
        user.setCreatedAt(LocalDateTime.now());
        
        // Create Collector entity
        Collector collector = new Collector();
        collector.setUser(user);
        collector.setUsername(request.getUsername());
        collector.setOwnedStickers(new HashMap<>());
        collector.setDailyStickerCount(0);
        collector.setStars(0);
        
        return collectorRepository.save(collector);
    }

    @Override
    @Transactional
    public Collector updateCollector(Long collectorId, CollectorRequest request) {
        log.info("Updating collector with ID: {}", collectorId);
        Collector collector = findById(collectorId);
        collector.setUsername(request.getUsername());
        return collectorRepository.save(collector);
    }

    @Override
    @Transactional
    public void deleteCollector(Long collectorId) {
        log.info("Deleting collector with ID: {}", collectorId);
        Collector collector = findById(collectorId);
        collectorRepository.delete(collector);
    }

    @Override
    public Page<Collector> getAllCollectorsWithPagination(Pageable pageable) {
        log.debug("Getting all collectors with pagination: page {}, size {}", 
                  pageable.getPageNumber(), pageable.getPageSize());
        return collectorRepository.findAll(pageable);
    }

    @Override
    public Page<Collector> searchCollectors(String search, Pageable pageable) {
        log.debug("Searching collectors with query '{}' with pagination: page {}, size {}", 
                  search, pageable.getPageNumber(), pageable.getPageSize());
        if (search == null || search.trim().isEmpty()) {
            return getAllCollectorsWithPagination(pageable);
        }
        return collectorRepository.searchCollectors(search, pageable);
    }
}
