package com.stickersafari.dto;

import com.stickersafari.enums.AlbumStatus;
import lombok.Data;

import java.util.List;

@Data
public class AlbumResponse {
    private Long id;
    private String name;
    private AlbumStatus status;
    private List<StickerResponse> stickers;
}
