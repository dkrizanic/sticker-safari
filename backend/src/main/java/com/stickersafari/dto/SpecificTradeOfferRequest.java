package com.stickersafari.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SpecificTradeOfferRequest {
    @NotNull(message = "Offered sticker ID is required")
    private Long offeredStickerId;
    
    @NotNull(message = "Received sticker ID is required")
    private Long receivedStickerId;
    
    @NotNull(message = "Sender ID is required")
    private Long senderId;
    
    @NotNull(message = "Receiver ID is required")
    private Long receiverId;
    
    private String message;
}
