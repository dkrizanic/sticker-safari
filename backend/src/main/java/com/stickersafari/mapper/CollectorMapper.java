package com.stickersafari.mapper;

import com.stickersafari.dto.CollectorResponse;
import com.stickersafari.dto.StickerWithQuantityResponse;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.service.StickerService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class CollectorMapper {
    
    @Autowired
    protected StickerService stickerService;
    
    @Autowired
    protected StickerMapper stickerMapper;
    
    @Mapping(target = "stickers", expression = "java(mapStickersWithQuantity(collector))")
    public abstract CollectorResponse toResponse(Collector collector);
    
    public abstract List<CollectorResponse> toResponseList(List<Collector> collectors);
    
    protected Set<StickerWithQuantityResponse> mapStickersWithQuantity(Collector collector) {
        Set<StickerWithQuantityResponse> result = new HashSet<>();
        if (collector.getOwnedStickers() != null) {
            collector.getOwnedStickers().forEach((stickerId, quantity) => {
                try {
                    Sticker sticker = stickerService.findOne(stickerId);
                    StickerWithQuantityResponse stickerWithQty = new StickerWithQuantityResponse();
                    stickerWithQty.setSticker(stickerMapper.toResponse(sticker));
                    stickerWithQty.setQuantity(quantity);
                    result.add(stickerWithQty);
                } catch (Exception e) {
                    // Skip stickers that can't be found
                }
            });
        }
        return result;
    }
}
