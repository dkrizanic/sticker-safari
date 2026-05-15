package com.stickersafari.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GlobalTradeOfferRequest {

    @NotNull(message = "Sender ID is required")
    private Long senderId;
    
    @NotNull(message = "Offered sticker ID is required")
    private Long offeredStickerId;
    
    @NotNull(message = "Received sticker ID is required")
    private Long receivedStickerId;
}
