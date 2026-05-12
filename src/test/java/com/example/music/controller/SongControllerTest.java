package com.example.music.controller;

import com.example.music.dto.SongDTO;
import com.example.music.service.PlayHistoryService;
import com.example.music.service.SongService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SongControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SongService songService;

    @Mock
    private PlayHistoryService playHistoryService;

    @InjectMocks
    private SongController songController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(songController).build();
    }

    private SongDTO createTestSongDTO() {
        return SongDTO.builder()
                .id(1L).title("Test Song").artist("Test Artist").album("Album")
                .genre("Pop").duration(180).playCount(100L).isFavorite(false).build();
    }

    @Test
    void getSongById_Found() throws Exception {
        when(songService.getSongById(eq(1L), any())).thenReturn(Optional.of(createTestSongDTO()));

        mockMvc.perform(get("/api/songs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Test Song"))
                .andExpect(jsonPath("$.data.artist").value("Test Artist"));
    }

    @Test
    void getSongById_NotFound() throws Exception {
        when(songService.getSongById(eq(999L), any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/songs/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTopSongs_Success() throws Exception {
        when(songService.getTopSongs(10, null)).thenReturn(List.of(createTestSongDTO()));

        mockMvc.perform(get("/api/songs/top").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].playCount").value(100));
    }

    @Test
    void getLatestSongs_Success() throws Exception {
        when(songService.getLatestSongs(10, null)).thenReturn(List.of(createTestSongDTO()));

        mockMvc.perform(get("/api/songs/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getAllGenres_Success() throws Exception {
        when(songService.getAllGenres()).thenReturn(List.of("Pop", "Rock"));

        mockMvc.perform(get("/api/songs/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("Pop"))
                .andExpect(jsonPath("$.data[1]").value("Rock"));
    }

    @Test
    void playSong_Success() throws Exception {
        doNothing().when(songService).incrementPlayCount(1L);

        mockMvc.perform(post("/api/songs/1/play"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(songService).incrementPlayCount(1L);
    }

    @Test
    void getSongById_WithAllFields() throws Exception {
        SongDTO song = SongDTO.builder()
                .id(2L).title("Full Song").artist("Artist").album("Album")
                .genre("Rock").duration(240).playCount(500L)
                .lyrics("Some lyrics").isFavorite(true).build();
        when(songService.getSongById(eq(2L), any())).thenReturn(Optional.of(song));

        mockMvc.perform(get("/api/songs/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Full Song"))
                .andExpect(jsonPath("$.data.isFavorite").value(true));
    }
}
