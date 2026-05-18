package com.stickersafari.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RankingFilterRequest extends PaginationRequest {
    private String search;
    
    public RankingFilterRequest(int page, int size, String search) {
        super(page, size);
        this.search = search;
    }
}
