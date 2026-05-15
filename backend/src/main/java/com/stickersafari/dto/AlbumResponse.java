package com.stickersafari.dto;

import lombok.Data;

import java.util.List;

@Data
public class AlbumResponse {
    private Long id;
    private String name;
    private List<StickerResponse> stickers;
}
