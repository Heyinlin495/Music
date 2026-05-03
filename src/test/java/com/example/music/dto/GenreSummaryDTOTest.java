package com.example.music.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenreSummaryDTOTest {

    @Test
    void builder_CreatesCorrectly() {
        GenreSummaryDTO dto = GenreSummaryDTO.builder()
                .genre("Pop").songCount(10L).coverUrl("cover.jpg").build();

        assertEquals("Pop", dto.getGenre());
        assertEquals(10L, dto.getSongCount());
        assertEquals("cover.jpg", dto.getCoverUrl());
    }
}
