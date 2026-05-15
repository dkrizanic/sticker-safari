package com.stickersafari.dto;

import lombok.Data;

@Data
public class StickerRequest {
    private String name;
    private String latinName;
    private String habitat;
    private String description;
    private String src;
    private Long albumId; // Optional: can be null
}
