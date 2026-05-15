package com.stickersafari.mapper;

import com.stickersafari.dto.AlbumResponse;
import com.stickersafari.entity.Album;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {StickerMapper.class})
public interface AlbumMapper {
    
    AlbumResponse toResponse(Album album);
    
    List<AlbumResponse> toResponseList(List<Album> albums);
}
