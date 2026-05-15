package com.stickersafari.dto;

import lombok.Data;

import java.util.List;

@Data
public class AlbumRequest {
    private String name;
    private List<Long> stickerIds; // Optional: existing stickers without album
}
