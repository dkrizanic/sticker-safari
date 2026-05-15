package com.stickersafari.service;

import com.stickersafari.entity.Collector;

import java.util.List;

public interface CollectorService {
    Collector findById(Long id);

    Collector findByUsername(String username);

    Collector save(Collector collector);

    List<Collector> findPotentialTradingPartners(Long offeredStickerId, Long receivedStickerId);
    
    void addStickersToCollector(Long collectorId, List<Long> stickerIds);
}
