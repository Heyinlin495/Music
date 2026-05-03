package com.example.music.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    void builder_CreatesCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        UserDTO dto = UserDTO.builder()
                .id(1L).username("user").email("e@e.com")
                .nickname("Nick").avatar("av.jpg").bio("Bio").createdAt(now).build();

        assertEquals(1L, dto.getId());
        assertEquals("user", dto.getUsername());
        assertEquals("e@e.com", dto.getEmail());
        assertEquals("Nick", dto.getNickname());
        assertEquals("av.jpg", dto.getAvatar());
        assertEquals("Bio", dto.getBio());
        assertEquals(now, dto.getCreatedAt());
    }
}
