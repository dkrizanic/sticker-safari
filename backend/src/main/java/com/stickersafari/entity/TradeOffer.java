package com.stickersafari.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "trade_offer")
public class TradeOffer {
    @EmbeddedId
    TradeOfferKey id;

    @ManyToOne
    @MapsId("senderId")
    @JoinColumn(name = "sender_id")
    private Collector sender;

    @ManyToOne
    @MapsId("receiverId")
    @JoinColumn(name = "receiver_id")
    private Collector receiver;

    @ManyToOne
    @MapsId("offeredStickerId")
    @JoinColumn(name = "offered_sticker_id")
    private Sticker offeredSticker;

    @ManyToOne
    @MapsId("receivedStickerId")
    @JoinColumn(name = "received_sticker_id")
    private Sticker receivedSticker;

    private String message;

    //PENDING, ACCEPTED, REFUSED
    private String status;
}
