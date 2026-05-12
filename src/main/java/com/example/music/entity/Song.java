package com.example.music.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "songs", indexes = {
    @Index(name = "idx_artist", columnList = "artist"),
    @Index(name = "idx_genre", columnList = "genre"),
    @Index(name = "idx_play_count", columnList = "play_count"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_uploader_id", columnList = "uploader_id"),
    @Index(name = "idx_title_artist", columnList = "title, artist")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    private String album;
    private String genre;
    private Integer duration; // in seconds
    private String coverUrl;
    private String fileUrl;
    @Column(columnDefinition = "TEXT")
    private String lyrics;

    @Column(name = "play_count")
    @Builder.Default
    private Long playCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    @JsonIgnoreProperties({"password", "favorites", "email", "bio", "enabled", "createdAt", "updatedAt"})
    private User uploader;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
