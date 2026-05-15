package com.stickersafari.service;

import com.stickersafari.dto.AlbumCompletionResponse;
import com.stickersafari.dto.AlbumRequest;
import com.stickersafari.entity.Album;
import com.stickersafari.entity.Sticker;

import java.util.List;

public interface AlbumService {

    List<Sticker> getMissingStickers (Album album, Long collectorId);
    Album getAlbum (Long albumId);
    AlbumCompletionResponse getAlbumCompletion(Long albumId, Long collectorId);
    
    // CRUD operations
    List<Album> getAllAlbums();
    Album createAlbum(AlbumRequest request);
    Album updateAlbum(Long albumId, AlbumRequest request);
    void deleteAlbum(Long albumId);
}
