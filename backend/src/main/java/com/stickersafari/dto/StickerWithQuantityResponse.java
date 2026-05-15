package com.stickersafari.dto;

import lombok.Data;

@Data
public class StickerWithQuantityResponse {

    private StickerResponse sticker;
    private Integer quantity;
}
