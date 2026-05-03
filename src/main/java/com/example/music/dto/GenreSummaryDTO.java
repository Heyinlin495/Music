package com.example.music.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreSummaryDTO {
    private String genre;
    private long songCount;
    private String coverUrl;
}
