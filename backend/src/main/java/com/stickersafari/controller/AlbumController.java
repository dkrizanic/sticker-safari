package com.stickersafari.controller;

import com.stickersafari.dto.AlbumCompletionResponse;
import com.stickersafari.dto.AlbumResponse;
import com.stickersafari.dto.StickerResponse;
import com.stickersafari.entity.Album;
import com.stickersafari.entity.Sticker;
import com.stickersafari.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("album")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping("/{albumId}")
    public AlbumResponse getAlbum(@PathVariable("albumId") Long albumId) {
        return mapAlbumToAlbumResponse(albumService.getAlbum(albumId));
    }

    @GetMapping("/{albumId}/collector/{collectorId}")
    public AlbumCompletionResponse getAlbumCompletion(@PathVariable("albumId") Long albumId,
                                                      @PathVariable("collectorId") Long collectorId) {
        Album album = albumService.getAlbum(albumId);

        List<Sticker> missingStickers = albumService.getMissingStickers(album, collectorId);

        int completion = 100 - ((missingStickers.size() / album.getStickers().size()) * 100);

        AlbumCompletionResponse albumCompletionResponse = new AlbumCompletionResponse();
        albumCompletionResponse.setCompletion((float) completion);
        albumCompletionResponse.setMissingStickerList(mapStickersToStickerResponses(missingStickers));

        return albumCompletionResponse;
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

    private AlbumResponse mapAlbumToAlbumResponse(Album album) {
        AlbumResponse albumResponse = new AlbumResponse();

        albumResponse.setId(album.getId());
        albumResponse.setName(album.getName());
        albumResponse.setStickers(mapStickersToStickerResponses(album.getStickers()));

        return albumResponse;
    }
}
