package com.stickersafari.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class CollectorStickerKey implements Serializable {

    @Column(name = "collector_id")
    private Long collectorId;

    @Column(name = "sticker_id")
    private Long stickerId;
}
