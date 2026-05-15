package com.stickersafari.service.impl;

import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.exception.EntityNotFoundException;
import com.stickersafari.repository.CollectorRepository;
import com.stickersafari.service.AlbumService;
import com.stickersafari.service.CollectorService;
import com.stickersafari.service.StickerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<Collector> findPotentialTradingPartners(Long offeredStickerId, Long receivedStickerId) {
        log.debug("Finding potential trading partners for offer: {} and request: {}", offeredStickerId, receivedStickerId);
        
        Sticker offeredSticker = stickerService.findOne(offeredStickerId);
        List<Collector> allCollectors = collectorRepository.findAll();

        return allCollectors.stream()
                .skip(1) // Skip first collector (original implementation started at index 1)
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
}
