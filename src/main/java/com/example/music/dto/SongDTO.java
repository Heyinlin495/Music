package com.example.music.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongDTO {
    private Long id;
    private String title;
    private String artist;
    private String album;
    private String genre;
    private Integer duration;
    private String coverUrl;
    private String fileUrl;
    private String lyrics;
    private Long playCount;
    private Long uploaderId;
    private String uploaderName;
    private LocalDateTime createdAt;
    private Boolean isFavorite;
}
