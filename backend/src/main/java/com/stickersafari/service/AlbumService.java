package com.stickersafari.service;

import com.stickersafari.entity.Album;
import com.stickersafari.entity.Sticker;

import java.util.List;

public interface AlbumService {

    List<Sticker> getMissingStickers (Album album, Long collectorId);
    Album getAlbum (Long albumId);
}
