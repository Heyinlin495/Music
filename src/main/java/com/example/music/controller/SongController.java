package com.example.music.controller;

import com.example.music.dto.AlbumDTO;
import com.example.music.dto.ApiResponse;
import com.example.music.dto.SongDTO;
import com.example.music.security.UserPrincipal;
import com.example.music.service.PlayHistoryService;
import com.example.music.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.music.dto.GenreSummaryDTO;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {
    
    private final SongService songService;
    private final PlayHistoryService playHistoryService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SongDTO>>> getAllSongs(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal user) {
        Long userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(songService.getAllSongs(pageable, userId)));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SongDTO>> getSongById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        Long userId = user != null ? user.getId() : null;
        return songService.getSongById(id, userId)
                .map(song -> ResponseEntity.ok(ApiResponse.success(song)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<SongDTO>>> searchSongs(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal user) {
        Long userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(songService.searchSongs(keyword, pageable, userId)));
    }
    
    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<SongDTO>>> getTopSongs(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal user) {
        Long userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(songService.getTopSongs(limit, userId)));
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<SongDTO>>> getLatestSongs(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal user) {
        Long userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(songService.getLatestSongs(limit, userId)));
    }

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<SongDTO>>> getDailySongs(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal user) {
        Long userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(songService.getDailySongs(limit, userId)));
    }

    @GetMapping("/random")
    public ResponseEntity<ApiResponse<List<SongDTO>>> getRandomSongs(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal user) {
        Long userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(songService.getRandomSongs(limit, userId)));
    }
    
    @GetMapping("/genres")
    public ResponseEntity<ApiResponse<List<String>>> getAllGenres() {
        return ResponseEntity.ok(ApiResponse.success(songService.getAllGenres()));
    }

    @GetMapping("/genres/summary")
    public ResponseEntity<ApiResponse<List<GenreSummaryDTO>>> getGenreSummaries() {
        return ResponseEntity.ok(ApiResponse.success(songService.getGenreSummaries()));
    }
    
    @GetMapping("/albums/recommended")
    public ResponseEntity<ApiResponse<Page<AlbumDTO>>> getRecommendedAlbums(
            @PageableDefault(size = 8) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(songService.getRecommendedAlbums(pageable)));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<ApiResponse<Page<SongDTO>>> getSongsByGenre(
            @PathVariable String genre,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal user) {
        Long userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(songService.getSongsByGenre(genre, pageable, userId)));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<SongDTO>> uploadSong(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            @RequestParam("title") String title,
            @RequestParam("artist") String artist,
            @RequestParam(value = "album", required = false) String album,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "duration", required = false) Integer duration,
            @RequestParam(value = "lyrics", required = false) String lyrics,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Title is required"));
            }
            if (artist == null || artist.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Artist is required"));
            }
            SongDTO song = songService.uploadSong(file, cover, title.trim(), artist.trim(), album, genre, duration, lyrics, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Song uploaded successfully", song));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to upload song: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/play")
    public ResponseEntity<ApiResponse<Void>> playSong(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        songService.incrementPlayCount(id);
        if (user != null) {
            playHistoryService.recordPlay(user.getId(), id);
        }
        return ResponseEntity.ok(ApiResponse.success("Play recorded", null));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSong(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            songService.deleteSong(id, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Song deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
