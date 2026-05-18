package com.stickersafari.service;

import com.stickersafari.dto.CollectorRequest;
import com.stickersafari.entity.Collector;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CollectorService {
    Collector findById(Long id);

    Collector findByUsername(String username);

    Collector save(Collector collector);

    List<Collector> findPotentialTradingPartners(Long senderId, Long offeredStickerId, Long receivedStickerId);
    
    void addStickersToCollector(Long collectorId, List<Long> stickerIds);
    
    void addStickerToCollector(Collector collector, Long stickerId);
    
    void removeStickerFromCollector(Collector collector, Long stickerId);
    
    // CRUD operations
    List<Collector> getAllCollectors();
    Collector createCollector(CollectorRequest request);
    Collector updateCollector(Long collectorId, CollectorRequest request, org.springframework.security.core.Authentication authentication);
    void deleteCollector(Long collectorId, org.springframework.security.core.Authentication authentication);
    
    // Pagination and search
    Page<Collector> getAllCollectorsWithPagination(Pageable pageable);
    Page<Collector> searchCollectors(String search, Pageable pageable);
    java.util.Map<String, Object> getCollectorsPaginated(int page, int size, String sortBy, String sortDir, String search);
    
    // Business logic
    List<Long> getOwnedStickerIds(Long collectorId);
    boolean canModifyCollector(Long collectorId, String username, boolean isAdmin);
}
