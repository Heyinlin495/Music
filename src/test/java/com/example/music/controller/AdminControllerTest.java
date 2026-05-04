package com.example.music.controller;

import com.example.music.dto.SongDTO;
import com.example.music.entity.Role;
import com.example.music.entity.Song;
import com.example.music.entity.User;
import com.example.music.service.AdminService;
import com.example.music.service.SongService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @Mock
    private SongService songService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    void getDashboard_Success() throws Exception {
        when(adminService.getDashboardStats()).thenReturn(Map.of(
                "totalUsers", 10L, "totalSongs", 50L, "totalPlaylists", 20L,
                "totalComments", 100L, "activeUsers", 8L, "adminCount", 2L, "totalPlays", 5000L));

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalSongs").value(50))
                .andExpect(jsonPath("$.totalPlays").value(5000));
    }

    @Test
    void getUser_Success() throws Exception {
        User admin = User.builder().id(1L).username("admin").build();
        when(adminService.getUserById(1L)).thenReturn(admin);

        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void updateUserRole_Success() throws Exception {
        when(adminService.updateUserRole(2L, Role.ADMIN)).thenReturn(
                User.builder().id(2L).role(Role.ADMIN).build());

        mockMvc.perform(put("/api/admin/users/2/role").param("role", "admin"))
                .andExpect(status().isOk());
    }

    @Test
    void toggleUserStatus_Success() throws Exception {
        when(adminService.toggleUserStatus(2L)).thenReturn(
                User.builder().id(2L).enabled(false).build());

        mockMvc.perform(put("/api/admin/users/2/toggle-status"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_Success() throws Exception {
        doNothing().when(adminService).deleteUser(2L);

        mockMvc.perform(delete("/api/admin/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    void updateSong_Success() throws Exception {
        SongDTO song = SongDTO.builder().id(1L).title("Updated").artist("New Artist").build();
        when(adminService.updateSong(1L, "Updated", "New Artist", null, null)).thenReturn(song);

        mockMvc.perform(put("/api/admin/songs/1")
                        .contentType("application/json")
                        .content("{\"title\":\"Updated\",\"artist\":\"New Artist\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void deleteSong_Success() throws Exception {
        doNothing().when(adminService).deleteSong(1L);

        mockMvc.perform(delete("/api/admin/songs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Song deleted successfully"));
    }

    @Test
    void createAdmin_MissingUsername() throws Exception {
        mockMvc.perform(post("/api/admin/users/create-admin")
                        .contentType("application/json")
                        .content("{\"password\":\"password\",\"email\":\"e@e.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username is required"));
    }

    @Test
    void createAdmin_ShortPassword() throws Exception {
        mockMvc.perform(post("/api/admin/users/create-admin")
                        .contentType("application/json")
                        .content("{\"username\":\"user\",\"password\":\"123\",\"email\":\"e@e.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password must be at least 6 characters"));
    }

    @Test
    void createAdmin_MissingEmail() throws Exception {
        mockMvc.perform(post("/api/admin/users/create-admin")
                        .contentType("application/json")
                        .content("{\"username\":\"user\",\"password\":\"password\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email is required"));
    }
}
