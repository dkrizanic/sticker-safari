package com.stickersafari.service.impl;

import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.repository.CollectorRepository;
import com.stickersafari.service.AlbumService;
import com.stickersafari.service.CollectorService;
import com.stickersafari.service.StickerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CollectorServiceImpl implements CollectorService {

    private final CollectorRepository collectorRepository;

    private final AlbumService albumService;

    private final StickerService stickerService;

    @Override
    public Collector findById(Long id) {
        return collectorRepository.findById(id).get();
    }

    @Override
    public Collector save(Collector collector) {
        return collectorRepository.save(collector);
    }

    @Override
    public List<Collector> findPotentialTradingPartners(Long offeredStickerId, Long receivedStickerId) {
        List<Collector> collectors = collectorRepository.findAll();

        for (int i = 1; i< collectors.size(); i++) {
            Collector collector = collectors.get(i);
            Sticker offeredSticker = stickerService.findOne(offeredStickerId);
            List<Sticker> receiverMissingStickers = albumService.getMissingStickers(offeredSticker.getAlbum(), collector.getId());

            boolean hasReceivedStickerDuplicate = collector.getOwnedStickers().containsKey(receivedStickerId) && collector.getOwnedStickers().get(receivedStickerId) > 0;
            boolean doesntHaveOfferedSticker = false;

            for (Sticker missingSticker : receiverMissingStickers) {
                if (missingSticker == offeredSticker) {
                    doesntHaveOfferedSticker = true;
                    break;
                }
            }

            if(!(hasReceivedStickerDuplicate && doesntHaveOfferedSticker)) {
                collectors.remove(i);
            }
        }

        return collectors;
    }
}
