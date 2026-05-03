package com.example.music.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthDTOTest {

    @Test
    void loginRequest_GettersAndSetters() {
        AuthDTO.LoginRequest request = new AuthDTO.LoginRequest();
        request.setUsername("user");
        request.setPassword("pass");

        assertEquals("user", request.getUsername());
        assertEquals("pass", request.getPassword());
    }

    @Test
    void loginRequest_AllArgsConstructor() {
        AuthDTO.LoginRequest request = new AuthDTO.LoginRequest("user", "pass", null, null);

        assertEquals("user", request.getUsername());
        assertEquals("pass", request.getPassword());
    }

    @Test
    void registerRequest_GettersAndSetters() {
        AuthDTO.RegisterRequest request = new AuthDTO.RegisterRequest();
        request.setUsername("user");
        request.setPassword("pass");
        request.setEmail("test@example.com");
        request.setNickname("Nick");

        assertEquals("user", request.getUsername());
        assertEquals("pass", request.getPassword());
        assertEquals("test@example.com", request.getEmail());
        assertEquals("Nick", request.getNickname());
    }

    @Test
    void registerRequest_AllArgsConstructor() {
        AuthDTO.RegisterRequest request = new AuthDTO.RegisterRequest("user", "pass", "e@e.com", "Nick");

        assertEquals("user", request.getUsername());
        assertEquals("pass", request.getPassword());
        assertEquals("e@e.com", request.getEmail());
        assertEquals("Nick", request.getNickname());
    }

    @Test
    void authResponse_Builder() {
        AuthDTO.AuthResponse response = AuthDTO.AuthResponse.builder()
                .token("jwt").type("Bearer").userId(1L)
                .username("user").nickname("Nick").avatar("av.jpg").build();

        assertEquals("jwt", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(1L, response.getUserId());
        assertEquals("user", response.getUsername());
        assertEquals("Nick", response.getNickname());
        assertEquals("av.jpg", response.getAvatar());
    }

    @Test
    void authResponse_AllArgsConstructor() {
        AuthDTO.AuthResponse response = new AuthDTO.AuthResponse(
                "jwt", "Bearer", 1L, "user", "Nick", "av.jpg", "user@test.com");

        assertEquals("jwt", response.getToken());
        assertEquals(1L, response.getUserId());
    }
}
