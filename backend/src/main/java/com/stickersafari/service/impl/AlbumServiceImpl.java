package com.stickersafari.service.impl;

import com.stickersafari.entity.Album;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.repository.AlbumRepository;
import com.stickersafari.service.AlbumService;
import com.stickersafari.service.CollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private final CollectorService collectorService;

    private final AlbumRepository albumRepository;

    @Override
    public List<Sticker> getMissingStickers(Album album, Long collectorId) {
        Collector collector = collectorService.findById(collectorId);

        return getUnownedStickersOfAlbum(album.getStickers(), collector.getOwnedStickers());
    }

    @Override
    public Album getAlbum(Long albumId) {
        return albumRepository.findById(albumId).get();
    }

    private List<Sticker> getUnownedStickersOfAlbum(List<Sticker> albumStickers, Map<Long, Integer> ownedStickers) {
        return albumStickers.stream()
                .filter((sticker) -> ownedStickers.containsKey(sticker.getId()))
                .collect(Collectors.toList());
    }
}
