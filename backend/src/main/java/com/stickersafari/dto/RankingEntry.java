package com.stickersafari.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankingEntry {
    private Long collectorId;
    private String username;
    private Long completions; // or stars for all-time
}
