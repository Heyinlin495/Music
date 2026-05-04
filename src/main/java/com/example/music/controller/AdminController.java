package com.example.music.controller;

import com.example.music.dto.PlaylistDTO;
import com.example.music.dto.SongDTO;
import com.example.music.entity.Role;
import com.example.music.entity.Song;
import com.example.music.entity.User;
import com.example.music.security.CurrentUser;
import com.example.music.security.UserPrincipal;
import com.example.music.service.AdminService;
import com.example.music.service.SongService;
import com.example.music.service.StorageService;
import com.example.music.util.FileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final AdminService adminService;
    private final SongService songService;
    private final StorageService storageService;

    @Value("${music.cover.path:./uploads/covers}")
    private String coverUploadPath;
    
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }
    
    // User Management
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(adminService.searchUsers(keyword, pageRequest));
        }
        return ResponseEntity.ok(adminService.getAllUsers(pageRequest));
    }
    
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }
    
    @PutMapping("/users/{id}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role) {
        Role newRole = Role.valueOf(role.toUpperCase());
        return ResponseEntity.ok(adminService.updateUserRole(id, newRole));
    }
    
    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<User> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleUserStatus(id));
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
    
    @PostMapping("/users/create-admin")
    public ResponseEntity<?> createAdmin(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String email = request.get("email");

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
        }
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        User admin = adminService.createAdmin(
                username.trim(),
                password,
                email.trim(),
                request.get("nickname")
        );
        return ResponseEntity.ok(admin);
    }
    
    // Song Management
    @GetMapping("/songs")
    public ResponseEntity<Page<SongDTO>> getSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(adminService.searchSongs(keyword, pageRequest));
        }
        return ResponseEntity.ok(adminService.getAllSongs(pageRequest));
    }
    
    @PostMapping("/songs/upload")
    public ResponseEntity<?> uploadSong(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("artist") String artist,
            @RequestParam(value = "album", required = false) String album,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            @RequestParam(value = "duration", required = false, defaultValue = "0") Integer duration) {
        
        try {
            var song = songService.uploadSong(file, cover, title, artist, album, genre, duration, null, currentUser.getId());
            return ResponseEntity.ok(song);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/songs/{id}")
    public ResponseEntity<SongDTO> updateSong(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        SongDTO song = adminService.updateSong(
                id,
                request.get("title"),
                request.get("artist"),
                request.get("album"),
                request.get("genre")
        );
        return ResponseEntity.ok(song);
    }
    
    @DeleteMapping("/songs/{id}")
    public ResponseEntity<?> deleteSong(@PathVariable Long id) {
        adminService.deleteSong(id);
        return ResponseEntity.ok(Map.of("message", "Song deleted successfully"));
    }

    // Playlist Management
    @GetMapping("/playlists")
    public ResponseEntity<Page<PlaylistDTO>> getPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(adminService.searchPlaylists(keyword, pageRequest));
        }
        return ResponseEntity.ok(adminService.getAllPlaylists(pageRequest));
    }

    @PostMapping("/playlists")
    public ResponseEntity<?> createPlaylist(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Boolean isPublic = (Boolean) request.get("isPublic");

            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Playlist name is required"));
            }

            Long userId = currentUser.getId();
            if (request.get("userId") != null) {
                userId = Long.valueOf(request.get("userId").toString());
            }

            PlaylistDTO playlist = adminService.createPlaylist(name, description, isPublic, userId);
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/playlists/{id}")
    public ResponseEntity<?> updatePlaylist(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Boolean isPublic = (Boolean) request.get("isPublic");
            String coverUrl = (String) request.get("coverUrl");

            PlaylistDTO playlist = adminService.updatePlaylist(id, name, description, isPublic, coverUrl);
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/playlists/{id}/cover")
    public ResponseEntity<?> uploadPlaylistCover(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            FileValidator.validateImageFile(file);
            String ext = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                    ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."))
                    : ".jpg";
            String filename = "playlist_" + UUID.randomUUID() + ext;
            Path dir = Paths.get(coverUploadPath).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path filePath = dir.resolve(filename);
            try (InputStream is = file.getInputStream()) {
                Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            String coverUrl = "/api/files/covers/" + filename;
            PlaylistDTO playlist = adminService.updatePlaylist(id, null, null, null, coverUrl);
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/playlists/{id}")
    public ResponseEntity<?> deletePlaylist(@PathVariable Long id) {
        try {
            adminService.deletePlaylist(id);
            return ResponseEntity.ok(Map.of("message", "Playlist deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/playlists/{id}")
    public ResponseEntity<?> getPlaylistDetail(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminService.getPlaylistDetail(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/playlists/{playlistId}/songs/{songId}")
    public ResponseEntity<?> addSongToPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId) {
        try {
            PlaylistDTO playlist = adminService.addSongToPlaylist(playlistId, songId);
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/playlists/{playlistId}/songs/{songId}")
    public ResponseEntity<?> removeSongFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId) {
        try {
            PlaylistDTO playlist = adminService.removeSongFromPlaylist(playlistId, songId);
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Check admin status
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkAdmin(@CurrentUser UserPrincipal currentUser) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("isAdmin", currentUser.isAdmin());
        return ResponseEntity.ok(response);
    }

    // Import local music files
    @PostMapping("/music/import")
    public ResponseEntity<?> importLocalMusic(@RequestParam(defaultValue = "/host") String directory) {
        try {
            Map<String, Object> result = adminService.importLocalMusic(directory);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Batch upload local music files from browser
    @PostMapping("/music/batch-upload")
    public ResponseEntity<?> batchUploadMusic(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("files") MultipartFile[] files) {

        int imported = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String filename = file.getOriginalFilename();
                if (filename == null || filename.isBlank()) {
                    errors.add("跳过空文件名");
                    continue;
                }

                // Parse artist and title from filename
                String nameWithoutExt = filename.contains(".")
                        ? filename.substring(0, filename.lastIndexOf('.'))
                        : filename;
                String artist;
                String title;
                if (nameWithoutExt.contains(" - ")) {
                    String[] parts = nameWithoutExt.split(" - ", 2);
                    artist = parts[0].trim();
                    title = parts[1].trim();
                } else {
                    artist = "未知歌手";
                    title = nameWithoutExt.trim();
                }

                songService.uploadSong(file, null, title, artist, "", "其他", 0, null, currentUser.getId());
                imported++;
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("歌曲已存在")) {
                    skipped++;
                } else {
                    errors.add(file.getOriginalFilename() + ": " + e.getMessage());
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("errors", errors);
        return ResponseEntity.ok(result);
    }
}
