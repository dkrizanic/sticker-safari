package com.stickersafari.dto;

import lombok.Data;

@Data
public class GlobalTradeOfferRequest {

    private Long senderId;
    private Long offeredStickerId;
    private Long receivedStickerId;
}
