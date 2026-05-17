package com.stickersafari.entity;

import com.stickersafari.enums.TradeOfferStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Objects;

@Entity
@Data
@Table(name = "trade_offer")
public class TradeOffer {
    @EmbeddedId
    private TradeOfferKey id;

    @ManyToOne
    @MapsId("senderId")
    @JoinColumn(name = "sender_id", nullable = false)
    @NotNull
    private Collector sender;

    @ManyToOne
    @MapsId("receiverId")
    @JoinColumn(name = "receiver_id", nullable = false)
    @NotNull
    private Collector receiver;

    @ManyToOne
    @MapsId("offeredStickerId")
    @JoinColumn(name = "offered_sticker_id", nullable = false)
    @NotNull
    private Sticker offeredSticker;

    @ManyToOne
    @MapsId("receivedStickerId")
    @JoinColumn(name = "received_sticker_id", nullable = false)
    @NotNull
    private Sticker receivedSticker;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private TradeOfferStatus status = TradeOfferStatus.PENDING;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TradeOffer)) return false;
        TradeOffer that = (TradeOffer) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "TradeOffer{" +
                "id=" + id +
                ", status=" + status +
                '}';
    }
}
