package com.stickersafari.service.impl;

import com.stickersafari.dto.CollectorRequest;
import com.stickersafari.dto.CollectorResponse;
import com.stickersafari.entity.Collector;
import com.stickersafari.enums.Role;
import com.stickersafari.entity.Sticker;
import com.stickersafari.entity.User;
import com.stickersafari.exception.EntityNotFoundException;
import com.stickersafari.mapper.CollectorMapper;
import com.stickersafari.repository.CollectorRepository;
import com.stickersafari.service.AlbumService;
import com.stickersafari.service.CollectorService;
import com.stickersafari.service.StickerService;
import com.stickersafari.util.PaginationResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final CollectorMapper collectorMapper;

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
            addStickerToCollector(collector, stickerId);
        }
        
        save(collector);
    }
    
    @Override
    public void addStickerToCollector(Collector collector, Long stickerId) {
        log.debug("Adding sticker {} to collector {}", stickerId, collector.getId());
        Map<Long, Integer> ownedStickers = collector.getOwnedStickers();
        
        if (ownedStickers.containsKey(stickerId)) {
            ownedStickers.put(stickerId, ownedStickers.get(stickerId) + 1);
        } else {
            ownedStickers.put(stickerId, 1);
        }
    }
    
    @Override
    public void removeStickerFromCollector(Collector collector, Long stickerId) {
        log.debug("Removing sticker {} from collector {}", stickerId, collector.getId());
        Map<Long, Integer> ownedStickers = collector.getOwnedStickers();
        
        if (!ownedStickers.containsKey(stickerId)) {
            throw new IllegalStateException("Cannot remove sticker " + stickerId + 
                " because " + collector.getUsername() + " does not have it");
        }
        
        Integer newValue = ownedStickers.get(stickerId) - 1;
        if (newValue > 0) {
            ownedStickers.put(stickerId, newValue);
        } else {
            ownedStickers.remove(stickerId);
        }
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
    public Collector updateCollector(Long collectorId, CollectorRequest request, org.springframework.security.core.Authentication authentication) {
        log.info("Updating collector with ID: {} by user: {}", collectorId, authentication.getName());
        
        // Check authorization - extract roles internally
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!canModifyCollector(collectorId, authentication.getName(), isAdmin)) {
            throw new org.springframework.security.access.AccessDeniedException(
                "You can only modify your own profile");
        }
        
        Collector collector = findById(collectorId);
        collector.setUsername(request.getUsername());
        return collectorRepository.save(collector);
    }

    @Override
    @Transactional
    public void deleteCollector(Long collectorId, org.springframework.security.core.Authentication authentication) {
        log.info("Deleting collector with ID: {} by user: {}", collectorId, authentication.getName());
        
        // Check authorization - extract roles internally
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!canModifyCollector(collectorId, authentication.getName(), isAdmin)) {
            throw new org.springframework.security.access.AccessDeniedException(
                "You can only delete your own profile");
        }
        
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
    
    @Override
    public List<Long> getOwnedStickerIds(Long collectorId) {
        log.debug("Getting owned sticker IDs for collector {}", collectorId);
        Collector collector = findById(collectorId);
        return new java.util.ArrayList<>(collector.getOwnedStickers().keySet());
    }
    
    @Override
    public boolean canModifyCollector(Long collectorId, String username, boolean isAdmin) {
        log.debug("Checking if user '{}' (admin: {}) can modify collector {}", username, isAdmin, collectorId);
        
        // Admins can modify any collector
        if (isAdmin) {
            return true;
        }
        
        // Collectors can only modify their own profile
        Collector collector = findById(collectorId);
        return collector.getUsername().equals(username);
    }
    
    @Override
    public Map<String, Object> getCollectorsPaginated(int page, int size, String sortBy, String sortDir, String search) {
        log.debug("Getting paginated collectors - page: {}, size: {}, sortBy: {}, sortDir: {}, search: {}", 
                  page, size, sortBy, sortDir, search);
        
        // Build pagination and sorting
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Get paginated data
        Page<Collector> collectorPage;
        if (search != null && !search.trim().isEmpty()) {
            collectorPage = searchCollectors(search, pageable);
        } else {
            collectorPage = getAllCollectorsWithPagination(pageable);
        }
        
        // Map to response DTOs
        List<CollectorResponse> collectors = collectorPage.getContent().stream()
                .map(collectorMapper::toResponse)
                .collect(Collectors.toList());
        
        // Build pagination response
        return PaginationResponseBuilder.build(collectors, collectorPage, "collectors");
    }
}
