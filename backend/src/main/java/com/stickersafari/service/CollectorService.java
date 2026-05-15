package com.stickersafari.service;

import com.stickersafari.entity.Collector;

import java.util.List;

public interface CollectorService {
    Collector findById(Long id);

    Collector save(Collector collector);

    List<Collector> findPotentialTradingPartners(Long offeredStickerId, Long receivedStickerId);
}
