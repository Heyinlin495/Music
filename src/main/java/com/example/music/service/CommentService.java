package com.example.music.service;

import com.example.music.dto.CommentDTO;
import com.example.music.entity.Comment;
import com.example.music.entity.Song;
import com.example.music.entity.User;
import com.example.music.repository.CommentRepository;
import com.example.music.repository.SongRepository;
import com.example.music.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    
    public Page<CommentDTO> getCommentsBySong(Long songId, Pageable pageable) {
        return commentRepository.findBySongIdOrderByCreatedAtDesc(songId, pageable)
                .map(this::convertToDTO);
    }
    
    @Transactional
    public CommentDTO addComment(Long songId, Long userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Comment content cannot be empty");
        }
        if (content.length() > 1000) {
            throw new RuntimeException("Comment content exceeds maximum length of 1000 characters");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));
        
        Comment comment = Comment.builder()
                .content(content)
                .user(user)
                .song(song)
                .build();
        
        comment = commentRepository.save(comment);
        return convertToDTO(comment);
    }
    
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own comments");
        }
        
        commentRepository.delete(comment);
    }
    
    public long getCommentCount(Long songId) {
        return commentRepository.countBySongId(songId);
    }
    
    private CommentDTO convertToDTO(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getNickname())
                .userAvatar(comment.getUser().getAvatar())
                .songId(comment.getSong().getId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
