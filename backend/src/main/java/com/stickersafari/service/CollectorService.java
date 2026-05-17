package com.stickersafari.service;

import com.stickersafari.dto.CollectorRequest;
import com.stickersafari.entity.Collector;

import java.util.List;

public interface CollectorService {
    Collector findById(Long id);

    Collector findByUsername(String username);

    Collector save(Collector collector);

    List<Collector> findPotentialTradingPartners(Long senderId, Long offeredStickerId, Long receivedStickerId);
    
    void addStickersToCollector(Long collectorId, List<Long> stickerIds);
    
    // CRUD operations
    List<Collector> getAllCollectors();
    Collector createCollector(CollectorRequest request);
    Collector updateCollector(Long collectorId, CollectorRequest request);
    void deleteCollector(Long collectorId);
}
