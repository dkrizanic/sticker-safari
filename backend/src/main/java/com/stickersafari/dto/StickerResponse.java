package com.stickersafari.dto;

import lombok.Data;

@Data
public class StickerResponse {
    private Long id;
    private String name;
    private String latinName;
    private String description;
    private String habitat;
    private String src;
    private Long albumId; // ID of the album (if assigned)

    private AlbumResponse album;
}
