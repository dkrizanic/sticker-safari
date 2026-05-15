package com.stickersafari.dto;

import lombok.Data;

import java.util.List;

@Data
public class AlbumCompletionResponse {
    private Float completion;

    private List<StickerResponse> missingStickerList;
}
