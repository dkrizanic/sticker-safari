package com.stickersafari.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelTradeOfferRequest {
    @NotNull(message = "Sender ID cannot be null")
    private Long senderId;
    
    @NotNull(message = "Receiver ID cannot be null")
    private Long receiverId;
    
    @NotNull(message = "Offered sticker ID cannot be null")
    private Long offeredStickerId;
    
    @NotNull(message = "Received sticker ID cannot be null")
    private Long receivedStickerId;
}
