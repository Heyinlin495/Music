package com.example.music.controller;

import com.example.music.dto.PlaylistDTO;
import com.example.music.service.PlaylistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PlaylistControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PlaylistService playlistService;

    @InjectMocks
    private PlaylistController playlistController;

    private PlaylistDTO testPlaylistDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(playlistController).build();
        testPlaylistDTO = PlaylistDTO.builder()
                .id(1L).name("Test Playlist").description("Desc").isPublic(true)
                .userId(1L).userName("Test User").songCount(5).build();
    }

    @Test
    void getFeaturedPlaylists_Success() throws Exception {
        when(playlistService.getFeaturedPlaylists(6)).thenReturn(List.of(testPlaylistDTO));

        mockMvc.perform(get("/api/playlists/featured").param("limit", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Test Playlist"));
    }

    @Test
    void getPlaylistById_Success() throws Exception {
        when(playlistService.getPlaylistById(eq(1L), any())).thenReturn(testPlaylistDTO);

        mockMvc.perform(get("/api/playlists/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test Playlist"))
                .andExpect(jsonPath("$.data.songCount").value(5));
    }

    @Test
    void getPlaylistById_NotFound() throws Exception {
        when(playlistService.getPlaylistById(eq(999L), any()))
                .thenThrow(new RuntimeException("Playlist not found"));

        mockMvc.perform(get("/api/playlists/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
