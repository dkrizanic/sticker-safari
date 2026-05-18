package com.stickersafari.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StickerFilterRequest extends PaginationRequest {
    private String search;
    private Long albumId;
    private String sortBy = "id";
    private String sortDir = "asc";
    
    public StickerFilterRequest(int page, int size, String search, Long albumId, String sortBy, String sortDir) {
        super(page, size);
        this.search = search;
        this.albumId = albumId;
        this.sortBy = sortBy;
        this.sortDir = sortDir;
    }
}
