package com.stickersafari.controller;

import com.stickersafari.dto.AlbumCompletionResponse;
import com.stickersafari.dto.AlbumResponse;
import com.stickersafari.mapper.AlbumMapper;
import com.stickersafari.service.AlbumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("album")
@RequiredArgsConstructor
@Slf4j
public class AlbumController {

    private final AlbumService albumService;
    private final AlbumMapper albumMapper;

    @GetMapping("/{albumId}")
    public AlbumResponse getAlbum(@PathVariable("albumId") Long albumId) {
        log.debug("Getting album with ID: {}", albumId);
        return albumMapper.toResponse(albumService.getAlbum(albumId));
    }

    @GetMapping("/{albumId}/collector/{collectorId}")
    public AlbumCompletionResponse getAlbumCompletion(@PathVariable("albumId") Long albumId,
                                                      @PathVariable("collectorId") Long collectorId) {
        log.debug("Getting album completion for album {} and collector {}", albumId, collectorId);
        return albumService.getAlbumCompletion(albumId, collectorId);
    }
}
