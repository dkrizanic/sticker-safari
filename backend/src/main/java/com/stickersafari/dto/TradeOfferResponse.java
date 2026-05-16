package com.stickersafari.dto;

import com.stickersafari.entity.TradeOfferStatus;
import lombok.Data;

@Data
public class TradeOfferResponse {
    private CollectorResponse sender;
    private CollectorResponse receiver;
    private StickerResponse offeredSticker;
    private StickerResponse receivedSticker;
    private String message;
    private TradeOfferStatus status;
}
