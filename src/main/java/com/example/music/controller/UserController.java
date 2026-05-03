package com.example.music.controller;

import com.example.music.dto.ApiResponse;
import com.example.music.dto.SongDTO;
import com.example.music.dto.UserDTO;
import com.example.music.security.UserPrincipal;
import com.example.music.service.PlayHistoryService;
import com.example.music.service.UserService;
import com.example.music.util.FileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final PlayHistoryService playHistoryService;
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(@AuthenticationPrincipal UserPrincipal user) {
        return userService.getUserById(user.getId())
                .map(u -> ResponseEntity.ok(ApiResponse.success(u)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(u -> ResponseEntity.ok(ApiResponse.success(u)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            String nickname = request.get("nickname");
            String bio = request.get("bio");
            String avatar = request.get("avatar");
            String email = request.get("email");

            UserDTO updated = userService.updateUser(user.getId(), nickname, bio, avatar, email);
            return ResponseEntity.ok(ApiResponse.success("Profile updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/me/avatar")
    public ResponseEntity<ApiResponse<UserDTO>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            FileValidator.validateImageFile(file);
            UserDTO updated = userService.updateAvatar(user.getId(), file);
            return ResponseEntity.ok(ApiResponse.success("Avatar updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");
            
            userService.changePassword(user.getId(), oldPassword, newPassword);
            return ResponseEntity.ok(ApiResponse.success("Password changed", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/me/favorites")
    public ResponseEntity<ApiResponse<List<SongDTO>>> getFavorites(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(userService.getFavorites(user.getId())));
    }
    
    @PostMapping("/me/favorites/{songId}")
    public ResponseEntity<ApiResponse<Void>> addFavorite(
            @PathVariable Long songId,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            userService.addFavorite(user.getId(), songId);
            return ResponseEntity.ok(ApiResponse.success("Added to favorites", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/me/favorites/{songId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable Long songId,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            userService.removeFavorite(user.getId(), songId);
            return ResponseEntity.ok(ApiResponse.success("Removed from favorites", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/me/history")
    public ResponseEntity<ApiResponse<Page<SongDTO>>> getPlayHistory(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(playHistoryService.getPlayHistory(user.getId(), pageable)));
    }

    @DeleteMapping("/me/history")
    public ResponseEntity<ApiResponse<Void>> clearPlayHistory(
            @AuthenticationPrincipal UserPrincipal user) {
        playHistoryService.clearPlayHistory(user.getId());
        return ResponseEntity.ok(ApiResponse.success("播放记录已清空", null));
    }

    @DeleteMapping("/me/history/{songId}")
    public ResponseEntity<ApiResponse<Void>> deleteHistoryItem(
            @PathVariable Long songId,
            @AuthenticationPrincipal UserPrincipal user) {
        playHistoryService.deleteHistoryItem(user.getId(), songId);
        return ResponseEntity.ok(ApiResponse.success("已删除", null));
    }

    @DeleteMapping("/me/history/batch")
    public ResponseEntity<ApiResponse<Void>> deleteHistoryItems(
            @RequestBody List<Long> songIds,
            @AuthenticationPrincipal UserPrincipal user) {
        playHistoryService.deleteHistoryItems(user.getId(), songIds);
        return ResponseEntity.ok(ApiResponse.success("已删除选中记录", null));
    }
}
