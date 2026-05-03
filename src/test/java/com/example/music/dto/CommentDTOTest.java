package com.example.music.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentDTOTest {

    @Test
    void builder_CreatesCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        CommentDTO dto = CommentDTO.builder()
                .id(1L).content("Great!").userId(1L).userName("User")
                .userAvatar("av.jpg").songId(1L).createdAt(now).build();

        assertEquals(1L, dto.getId());
        assertEquals("Great!", dto.getContent());
        assertEquals(1L, dto.getUserId());
        assertEquals("User", dto.getUserName());
        assertEquals("av.jpg", dto.getUserAvatar());
        assertEquals(1L, dto.getSongId());
        assertEquals(now, dto.getCreatedAt());
    }
}
