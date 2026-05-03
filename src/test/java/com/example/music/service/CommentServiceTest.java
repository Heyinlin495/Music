package com.example.music.service;

import com.example.music.dto.CommentDTO;
import com.example.music.entity.Comment;
import com.example.music.entity.Song;
import com.example.music.entity.User;
import com.example.music.entity.Role;
import com.example.music.repository.CommentRepository;
import com.example.music.repository.SongRepository;
import com.example.music.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Song testSong;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).username("testuser").nickname("Test User").role(Role.USER).build();
        testSong = Song.builder()
                .id(1L).title("Test Song").artist("Artist").build();
        testComment = Comment.builder()
                .id(1L).content("Great song!").user(testUser).song(testSong).build();
    }

    @Test
    void getCommentsBySong_ReturnsPage() {
        Page<Comment> page = new PageImpl<>(List.of(testComment));
        when(commentRepository.findBySongIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        Page<CommentDTO> result = commentService.getCommentsBySong(1L, PageRequest.of(0, 20));

        assertEquals(1, result.getContent().size());
        assertEquals("Great song!", result.getContent().get(0).getContent());
    }

    @Test
    void addComment_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        CommentDTO result = commentService.addComment(1L, 1L, "Great song!");

        assertNotNull(result);
        assertEquals("Great song!", result.getContent());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_EmptyContent_ThrowsException() {
        assertThrows(RuntimeException.class,
                () -> commentService.addComment(1L, 1L, ""));
    }

    @Test
    void addComment_NullContent_ThrowsException() {
        assertThrows(RuntimeException.class,
                () -> commentService.addComment(1L, 1L, null));
    }

    @Test
    void addComment_ContentTooLong_ThrowsException() {
        String longContent = "a".repeat(1001);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> commentService.addComment(1L, 1L, longContent));
        assertTrue(ex.getMessage().contains("maximum length"));
    }

    @Test
    void addComment_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> commentService.addComment(1L, 999L, "Comment"));
    }

    @Test
    void addComment_SongNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> commentService.addComment(999L, 1L, "Comment"));
    }

    @Test
    void deleteComment_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        commentService.deleteComment(1L, 1L);

        verify(commentRepository).delete(testComment);
    }

    @Test
    void deleteComment_NotOwner_ThrowsException() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        assertThrows(RuntimeException.class,
                () -> commentService.deleteComment(1L, 2L));
    }

    @Test
    void deleteComment_NotFound_ThrowsException() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> commentService.deleteComment(999L, 1L));
    }

    @Test
    void getCommentCount_ReturnsCount() {
        when(commentRepository.countBySongId(1L)).thenReturn(5L);

        long count = commentService.getCommentCount(1L);

        assertEquals(5L, count);
    }
}
