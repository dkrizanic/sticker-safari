package com.stickersafari.dto;

import com.stickersafari.enums.AlbumStatus;
import lombok.Data;

import java.util.List;

@Data
public class AlbumRequest {
    private String name;
    private AlbumStatus status; // Optional: manually set album status
    private List<Long> stickerIds; // Optional: existing stickers without album
}
