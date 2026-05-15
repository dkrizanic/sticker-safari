package com.stickersafari.dto;

import lombok.Data;

import java.util.Set;

@Data
public class CollectorResponse {

    private String username;
    private Set<StickerWithQuantityResponse> stickers;
}
