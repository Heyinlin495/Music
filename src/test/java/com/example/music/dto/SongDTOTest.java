package com.example.music.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SongDTOTest {

    @Test
    void builder_CreatesCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        SongDTO dto = SongDTO.builder()
                .id(1L).title("Song").artist("Artist").album("Album")
                .genre("Pop").duration(180).coverUrl("cover.jpg")
                .fileUrl("file.mp3").songmid("abc123").lyrics("Lyrics")
                .playCount(100L).uploaderId(1L).uploaderName("User")
                .createdAt(now).isFavorite(true).build();

        assertEquals(1L, dto.getId());
        assertEquals("Song", dto.getTitle());
        assertEquals("Artist", dto.getArtist());
        assertEquals("Album", dto.getAlbum());
        assertEquals("Pop", dto.getGenre());
        assertEquals(180, dto.getDuration());
        assertEquals("cover.jpg", dto.getCoverUrl());
        assertEquals("file.mp3", dto.getFileUrl());
        assertEquals("abc123", dto.getSongmid());
        assertEquals("Lyrics", dto.getLyrics());
        assertEquals(100L, dto.getPlayCount());
        assertEquals(1L, dto.getUploaderId());
        assertEquals("User", dto.getUploaderName());
        assertEquals(now, dto.getCreatedAt());
        assertTrue(dto.getIsFavorite());
    }

    @Test
    void noArgsConstructor() {
        SongDTO dto = new SongDTO();

        assertNull(dto.getTitle());
        assertNull(dto.getId());
    }

    @Test
    void setters_Work() {
        SongDTO dto = new SongDTO();
        dto.setId(1L);
        dto.setTitle("Test");

        assertEquals(1L, dto.getId());
        assertEquals("Test", dto.getTitle());
    }
}
