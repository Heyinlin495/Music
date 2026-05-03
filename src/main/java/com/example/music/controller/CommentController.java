package com.example.music.controller;

import com.example.music.dto.ApiResponse;
import com.example.music.dto.CommentDTO;
import com.example.music.security.UserPrincipal;
import com.example.music.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    @GetMapping("/song/{songId}")
    public ResponseEntity<ApiResponse<Page<CommentDTO>>> getCommentsBySong(
            @PathVariable Long songId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getCommentsBySong(songId, pageable)));
    }
    
    @PostMapping("/song/{songId}")
    public ResponseEntity<ApiResponse<CommentDTO>> addComment(
            @PathVariable Long songId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            String content = request.get("content");
            CommentDTO comment = commentService.addComment(songId, user.getId(), content);
            return ResponseEntity.ok(ApiResponse.success("Comment added", comment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            commentService.deleteComment(id, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Comment deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
