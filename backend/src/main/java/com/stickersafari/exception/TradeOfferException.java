package com.stickersafari.exception;

/**
 * Exception thrown when a trade offer operation fails
 */
public class TradeOfferException extends RuntimeException {
    
    public TradeOfferException(String message) {
        super(message);
    }
    
    public TradeOfferException(String message, Throwable cause) {
        super(message, cause);
    }
}
