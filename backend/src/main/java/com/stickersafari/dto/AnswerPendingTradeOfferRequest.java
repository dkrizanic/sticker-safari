package com.stickersafari.dto;

import lombok.Data;

@Data
public class AnswerPendingTradeOfferRequest {
    private Long offeredStickerId;
    private Long receivedStickerId;
    private Long senderId;
    private Long receiverId;
}
