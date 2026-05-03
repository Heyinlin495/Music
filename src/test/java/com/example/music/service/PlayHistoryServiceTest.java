package com.example.music.service;

import com.example.music.dto.SongDTO;
import com.example.music.entity.PlayHistory;
import com.example.music.entity.Song;
import com.example.music.entity.User;
import com.example.music.entity.Role;
import com.example.music.repository.PlayHistoryRepository;
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
class PlayHistoryServiceTest {

    @Mock
    private PlayHistoryRepository playHistoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private PlayHistoryService playHistoryService;

    private User testUser;
    private Song testSong;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).username("testuser").nickname("Test User").role(Role.USER).build();
        testSong = Song.builder()
                .id(1L).title("Test Song").artist("Artist").playCount(0L).build();
    }

    @Test
    void recordPlay_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(playHistoryRepository.save(any(PlayHistory.class))).thenReturn(new PlayHistory());

        playHistoryService.recordPlay(1L, 1L);

        verify(playHistoryRepository).save(any(PlayHistory.class));
    }

    @Test
    void recordPlay_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> playHistoryService.recordPlay(999L, 1L));
    }

    @Test
    void recordPlay_SongNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> playHistoryService.recordPlay(1L, 999L));
    }

    @Test
    void getPlayHistory_ReturnsPage() {
        PlayHistory history = PlayHistory.builder().user(testUser).song(testSong).build();
        Page<PlayHistory> page = new PageImpl<>(List.of(history));
        when(playHistoryRepository.findByUserIdOrderByPlayedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        Page<SongDTO> result = playHistoryService.getPlayHistory(1L, PageRequest.of(0, 20));

        assertEquals(1, result.getContent().size());
        assertEquals("Test Song", result.getContent().get(0).getTitle());
    }

    @Test
    void getPlayHistory_Empty_ReturnsEmptyPage() {
        Page<PlayHistory> page = new PageImpl<>(List.of());
        when(playHistoryRepository.findByUserIdOrderByPlayedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        Page<SongDTO> result = playHistoryService.getPlayHistory(1L, PageRequest.of(0, 20));

        assertTrue(result.getContent().isEmpty());
    }
}
