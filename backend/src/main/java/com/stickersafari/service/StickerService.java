package com.stickersafari.service;

import com.stickersafari.entity.Sticker;

import java.util.List;

public interface StickerService {

    Sticker findOne(Long stickerId);

    List<Sticker> getStickersOfAlbum(Long albumId);
}
