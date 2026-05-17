package com.stickersafari.mapper;

import com.stickersafari.dto.StickerResponse;
import com.stickersafari.entity.Sticker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StickerMapper {
    
    @Mapping(target = "album", ignore = true)
    StickerResponse toResponse(Sticker sticker);
    
    List<StickerResponse> toResponseList(List<Sticker> stickers);
}
