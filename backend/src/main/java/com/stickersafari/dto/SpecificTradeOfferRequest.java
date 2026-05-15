package com.stickersafari.dto;

import lombok.Data;

@Data
public class SpecificTradeOfferRequest {
    private Long offeredStickerId;
    private Long receivedStickerId;
    private Long senderId;
    private Long receiverId;
    private String message;
}
