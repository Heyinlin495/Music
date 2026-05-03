package com.example.music.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaylistDTO {
    private Long id;
    private String name;
    private String description;
    private String coverUrl;
    private Boolean isPublic;
    private Long userId;
    private String userName;
    private Integer songCount;
    private List<SongDTO> songs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
