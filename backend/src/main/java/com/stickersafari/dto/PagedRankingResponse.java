package com.stickersafari.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedRankingResponse {
    private List<RankingEntry> rankings;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
}
