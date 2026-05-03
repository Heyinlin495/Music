package com.example.music.controller;

import com.example.music.dto.UserDTO;
import com.example.music.service.PlayHistoryService;
import com.example.music.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private PlayHistoryService playHistoryService;

    @InjectMocks
    private UserController userController;

    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                
                .build();
        testUserDTO = UserDTO.builder()
                .id(1L).username("testuser").email("test@example.com")
                .nickname("Test User").createdAt(LocalDateTime.now()).build();
    }

    @Test
    void getUserById_Success() throws Exception {
        when(userService.getUserById(2L)).thenReturn(Optional.of(testUserDTO));

        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void getUserById_NotFound() throws Exception {
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserById_AnotherUser() throws Exception {
        UserDTO another = UserDTO.builder()
                .id(5L).username("another").nickname("Another").build();
        when(userService.getUserById(5L)).thenReturn(Optional.of(another));

        mockMvc.perform(get("/api/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("another"));
    }
}
