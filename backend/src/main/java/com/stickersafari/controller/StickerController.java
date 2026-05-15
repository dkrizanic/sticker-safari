package com.stickersafari.controller;

import com.stickersafari.dto.StickerResponse;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.repository.CollectorRepository;
import com.stickersafari.service.StickerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("stickers")
@RequiredArgsConstructor
public class StickerController {

    private final StickerService stickerService;

    private final CollectorRepository collectorRepository;

    @GetMapping("/open/{albumId}/collector/{userId}")
    public List<StickerResponse> openPack(@PathVariable("albumId") Long albumId, 
                                           @PathVariable("userId") Long userId,
                                           @RequestParam(value = "size", defaultValue = "3") int size) {
        List<Sticker> albumStickers = stickerService.getStickersOfAlbum(albumId);
        List<Sticker> randomStickers = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < size; i++) {
            int randomIndex = random.nextInt(albumStickers.size());
            Sticker randomSticker = albumStickers.get(randomIndex);
            randomStickers.add(randomSticker);
        }

        List<StickerResponse> stickerResponses = mapStickersToStickerResponses(randomStickers);

        Collector collector = collectorRepository.findById(userId).get();

        Map<Long, Integer> ownedStickers = collector.getOwnedStickers();

        ownedStickers = updateCollectorStickers(ownedStickers, stickerResponses);
        collector.setOwnedStickers(ownedStickers);
        collectorRepository.save(collector);

        return stickerResponses;
    }

    private List<StickerResponse> mapStickersToStickerResponses(List<Sticker> stickers) {
        List<StickerResponse> stickerResponses = new ArrayList<>();

        for (Sticker sticker : stickers) {
            StickerResponse stickerResponse = new StickerResponse();

            stickerResponse.setName(sticker.getName());
            stickerResponse.setLatinName(sticker.getLatinName());
            stickerResponse.setHabitat(sticker.getHabitat());
            stickerResponse.setDescription(sticker.getDescription());
            stickerResponse.setId(sticker.getId());
            stickerResponse.setSrc(sticker.getSrc());

            stickerResponses.add(stickerResponse);
        }

        return stickerResponses;
    }

    private Map<Long, Integer> updateCollectorStickers(Map<Long, Integer> ownedStickers, List<StickerResponse> stickers) {
        for (StickerResponse sticker : stickers) {
            if (ownedStickers.containsKey(sticker.getId())){
                ownedStickers.put(sticker.getId(), ownedStickers.get(sticker.getId()) + 1);
            } else {
                ownedStickers.put(sticker.getId(), 1);
            }
        }
        return ownedStickers;
    }
}
