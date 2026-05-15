package com.stickersafari.mapper;

import com.stickersafari.dto.TradeOfferResponse;
import com.stickersafari.entity.TradeOffer;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {StickerMapper.class, CollectorMapper.class})
public interface TradeOfferMapper {
    
    TradeOfferResponse toResponse(TradeOffer tradeOffer);
    
    List<TradeOfferResponse> toResponseList(List<TradeOffer> tradeOffers);
}
