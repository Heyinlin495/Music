package com.example.music.controller;

import com.example.music.dto.AuthDTO;
import com.example.music.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                
                .build();
    }

    @Test
    void login_Success() throws Exception {
        AuthDTO.LoginRequest request = new AuthDTO.LoginRequest("testuser", "password", null, null);
        AuthDTO.AuthResponse response = AuthDTO.AuthResponse.builder()
                .token("jwt-token").type("Bearer").userId(1L)
                .username("testuser").nickname("Test User").build();

        when(authService.login(any(AuthDTO.LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void login_BadCredentials() throws Exception {
        AuthDTO.LoginRequest request = new AuthDTO.LoginRequest("testuser", "wrong", null, null);

        when(authService.login(any(AuthDTO.LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_Success() throws Exception {
        AuthDTO.RegisterRequest request = new AuthDTO.RegisterRequest("newuser", "password", "new@example.com", "New User");
        AuthDTO.AuthResponse response = AuthDTO.AuthResponse.builder()
                .token("jwt-token").type("Bearer").userId(2L)
                .username("newuser").nickname("New User").build();

        when(authService.register(any(AuthDTO.RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(2));
    }

    @Test
    void register_UsernameTaken() throws Exception {
        AuthDTO.RegisterRequest request = new AuthDTO.RegisterRequest("existing", "password", "new@example.com", "Name");

        when(authService.register(any(AuthDTO.RegisterRequest.class)))
                .thenThrow(new RuntimeException("Username is already taken"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username is already taken"));
    }
}
