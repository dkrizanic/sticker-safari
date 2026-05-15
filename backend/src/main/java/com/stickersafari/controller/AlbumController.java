package com.stickersafari.controller;

import com.stickersafari.dto.AlbumCompletionResponse;
import com.stickersafari.dto.AlbumRequest;
import com.stickersafari.dto.AlbumResponse;
import com.stickersafari.entity.Album;
import com.stickersafari.mapper.AlbumMapper;
import com.stickersafari.service.AlbumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("album")
@RequiredArgsConstructor
@Slf4j
public class AlbumController {

    private final AlbumService albumService;
    private final AlbumMapper albumMapper;

    @GetMapping
    public ResponseEntity<List<AlbumResponse>> getAllAlbums() {
        log.debug("Getting all albums");
        List<Album> albums = albumService.getAllAlbums();
        return ResponseEntity.ok(albumMapper.toResponseList(albums));
    }

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

    @PostMapping
    public ResponseEntity<AlbumResponse> createAlbum(@RequestBody AlbumRequest request) {
        log.info("Creating album: {}", request.getName());
        Album album = albumService.createAlbum(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(albumMapper.toResponse(album));
    }

    @PutMapping("/{albumId}")
    public ResponseEntity<AlbumResponse> updateAlbum(@PathVariable("albumId") Long albumId,
                                                      @RequestBody AlbumRequest request) {
        log.info("Updating album with ID: {}", albumId);
        Album album = albumService.updateAlbum(albumId, request);
        return ResponseEntity.ok(albumMapper.toResponse(album));
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable("albumId") Long albumId) {
        log.info("Deleting album with ID: {}", albumId);
        albumService.deleteAlbum(albumId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{albumId}/complete/collector/{collectorId}")
    public ResponseEntity<Void> completeAlbum(@PathVariable("albumId") Long albumId,
                                               @PathVariable("collectorId") Long collectorId) {
        log.info("Collector {} completing album {}", collectorId, albumId);
        albumService.completeAlbum(albumId, collectorId);
        return ResponseEntity.ok().build();
    }
}
