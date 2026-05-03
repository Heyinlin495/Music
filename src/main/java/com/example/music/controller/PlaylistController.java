package com.example.music.controller;

import com.example.music.dto.ApiResponse;
import com.example.music.dto.PlaylistDTO;
import com.example.music.security.UserPrincipal;
import com.example.music.service.PlaylistService;
import com.example.music.util.FileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @Value("${music.cover.path}")
    private String coverUploadPath;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PlaylistDTO>>> getPublicPlaylists(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.getPublicPlaylists(pageable)));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<PlaylistDTO>>> getFeaturedPlaylists(
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.getFeaturedPlaylists(limit)));
    }
    
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PlaylistDTO>>> getMyPlaylists(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.getUserPlaylists(user.getId())));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlaylistDTO>> getPlaylistById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            Long userId = user != null ? user.getId() : null;
            return ResponseEntity.ok(ApiResponse.success(playlistService.getPlaylistById(id, userId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<PlaylistDTO>> createPlaylist(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Boolean isPublic = (Boolean) request.get("isPublic");
            
            PlaylistDTO playlist = playlistService.createPlaylist(name, description, isPublic, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Playlist created", playlist));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PlaylistDTO>> updatePlaylist(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Boolean isPublic = (Boolean) request.get("isPublic");
            String coverUrl = (String) request.get("coverUrl");

            PlaylistDTO playlist = playlistService.updatePlaylist(id, name, description, isPublic, coverUrl, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Playlist updated", playlist));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/cover")
    public ResponseEntity<ApiResponse<PlaylistDTO>> uploadCover(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal user) {
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
            PlaylistDTO playlist = playlistService.updatePlaylist(id, null, null, null, coverUrl, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Cover uploaded", playlist));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlaylist(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            playlistService.deletePlaylist(id, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Playlist deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/songs/{songId}")
    public ResponseEntity<ApiResponse<PlaylistDTO>> addSongToPlaylist(
            @PathVariable Long id,
            @PathVariable Long songId,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            PlaylistDTO playlist = playlistService.addSongToPlaylist(id, songId, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Song added to playlist", playlist));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}/songs/{songId}")
    public ResponseEntity<ApiResponse<PlaylistDTO>> removeSongFromPlaylist(
            @PathVariable Long id,
            @PathVariable Long songId,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            PlaylistDTO playlist = playlistService.removeSongFromPlaylist(id, songId, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Song removed from playlist", playlist));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
