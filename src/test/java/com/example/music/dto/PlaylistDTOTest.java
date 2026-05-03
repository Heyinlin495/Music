package com.example.music.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlaylistDTOTest {

    @Test
    void builder_CreatesCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        SongDTO song = SongDTO.builder().id(1L).title("Song").build();
        PlaylistDTO dto = PlaylistDTO.builder()
                .id(1L).name("Playlist").description("Desc").coverUrl("cover.jpg")
                .isPublic(true).userId(1L).userName("User").songCount(1)
                .songs(List.of(song)).createdAt(now).updatedAt(now).build();

        assertEquals(1L, dto.getId());
        assertEquals("Playlist", dto.getName());
        assertEquals("Desc", dto.getDescription());
        assertEquals("cover.jpg", dto.getCoverUrl());
        assertTrue(dto.getIsPublic());
        assertEquals(1L, dto.getUserId());
        assertEquals("User", dto.getUserName());
        assertEquals(1, dto.getSongCount());
        assertEquals(1, dto.getSongs().size());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }
}
