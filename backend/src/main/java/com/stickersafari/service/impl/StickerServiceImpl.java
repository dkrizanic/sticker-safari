package com.stickersafari.service.impl;

import com.stickersafari.entity.Sticker;
import com.stickersafari.repository.StickerRepository;
import com.stickersafari.service.StickerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StickerServiceImpl implements StickerService {

    private final StickerRepository stickerRepository;

    public StickerServiceImpl(StickerRepository stickerRepository) {
        this.stickerRepository = stickerRepository;
    }

    @Override
    public Sticker findOne(Long stickerId) {
        List<Sticker> allStickers = stickerRepository.findAll();

        for (final Sticker sticker : allStickers) {
            if (sticker.getId() == stickerId) {
                return sticker;
            }
        }

        return null;
    }

    @Override
    public List<Sticker> getStickersOfAlbum(Long albumId) {
        return stickerRepository.getStickersOfAlbum(albumId);
    }
}
