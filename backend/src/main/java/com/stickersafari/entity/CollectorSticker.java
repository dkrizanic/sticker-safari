package com.stickersafari.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "collector_sticker")
public class CollectorSticker {

    @EmbeddedId
    private CollectorStickerKey id;

    @ManyToOne
    @MapsId("collectorId")
    @JoinColumn(name = "collector_id")
    private Collector collector;

    @ManyToOne
    @MapsId("stickerId")
    @JoinColumn(name = "sticker_id")
    private Sticker sticker;

    private Integer quantity;

}
