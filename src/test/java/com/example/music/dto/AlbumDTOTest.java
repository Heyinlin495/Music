package com.example.music.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlbumDTOTest {

    @Test
    void builder_CreatesCorrectly() {
        AlbumDTO dto = AlbumDTO.builder()
                .album("Test Album").artist("Artist").coverUrl("cover.jpg").songCount(5L).build();

        assertEquals("Test Album", dto.getAlbum());
        assertEquals("Artist", dto.getArtist());
        assertEquals("cover.jpg", dto.getCoverUrl());
        assertEquals(5L, dto.getSongCount());
    }

    @Test
    void noArgsConstructor() {
        AlbumDTO dto = new AlbumDTO();

        assertNull(dto.getAlbum());
        assertNull(dto.getSongCount());
    }
}
