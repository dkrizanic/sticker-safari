package com.stickersafari.mapper;

import com.stickersafari.dto.StickerResponse;
import com.stickersafari.entity.Sticker;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StickerMapper {
    
    StickerResponse toResponse(Sticker sticker);
    
    List<StickerResponse> toResponseList(List<Sticker> stickers);
}
