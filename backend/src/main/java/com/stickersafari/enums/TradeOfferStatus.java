package com.stickersafari.enums;

/**
 * Enum representing the status of a trade offer
 */
public enum TradeOfferStatus {
    /**
     * Trade offer has been sent but not yet responded to
     */
    PENDING,
    
    /**
     * Trade offer was accepted by the receiver
     */
    ACCEPTED,
    
    /**
     * Trade offer was refused by the receiver
     */
    REFUSED
}
