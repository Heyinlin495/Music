package com.example.music.service;

import com.example.music.dto.AlbumDTO;
import com.example.music.dto.GenreSummaryDTO;
import com.example.music.dto.SongDTO;
import com.example.music.entity.Song;
import com.example.music.entity.User;
import com.example.music.entity.Role;
import com.example.music.repository.SongRepository;
import com.example.music.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SongServiceTest {

    @Mock
    private SongRepository songRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private SongService songService;

    private User testUser;
    private Song testSong;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).username("testuser").nickname("Test User").role(Role.USER).build();
        testSong = Song.builder()
                .id(1L).title("Test Song").artist("Test Artist").album("Test Album")
                .genre("Pop").duration(180).playCount(100L).uploader(testUser).build();
    }

    @Test
    void getAllSongs_ReturnsPage() {
        Page<Song> songPage = new PageImpl<>(List.of(testSong));
        when(songRepository.findAll(any(Pageable.class))).thenReturn(songPage);
        when(userRepository.findByIdWithFavorites(1L)).thenReturn(Optional.of(testUser));

        Page<SongDTO> result = songService.getAllSongs(PageRequest.of(0, 20), 1L);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Song", result.getContent().get(0).getTitle());
    }

    @Test
    void getAllSongs_NullUserId() {
        Page<Song> songPage = new PageImpl<>(List.of(testSong));
        when(songRepository.findAll(any(Pageable.class))).thenReturn(songPage);

        Page<SongDTO> result = songService.getAllSongs(PageRequest.of(0, 20), null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertFalse(result.getContent().get(0).getIsFavorite());
    }

    @Test
    void getSongById_Found() {
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(userRepository.findByIdWithFavorites(1L)).thenReturn(Optional.of(testUser));

        Optional<SongDTO> result = songService.getSongById(1L, 1L);

        assertTrue(result.isPresent());
        assertEquals("Test Song", result.get().getTitle());
        assertEquals("Test Artist", result.get().getArtist());
    }

    @Test
    void getSongById_NotFound() {
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<SongDTO> result = songService.getSongById(999L, null);

        assertFalse(result.isPresent());
    }

    @Test
    void searchSongs_ReturnsResults() {
        Page<Song> songPage = new PageImpl<>(List.of(testSong));
        when(songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
                eq("Test"), eq("Test"), any(Pageable.class))).thenReturn(songPage);
        when(userRepository.findByIdWithFavorites(1L)).thenReturn(Optional.of(testUser));

        Page<SongDTO> result = songService.searchSongs("Test", PageRequest.of(0, 20), 1L);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getTopSongs_ReturnsList() {
        when(songRepository.findTopByPlayCount(any(PageRequest.class))).thenReturn(List.of(testSong));
        when(userRepository.findByIdWithFavorites(1L)).thenReturn(Optional.of(testUser));

        List<SongDTO> result = songService.getTopSongs(10, 1L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getPlayCount());
    }

    @Test
    void getLatestSongs_ReturnsList() {
        when(songRepository.findLatestSongs(any(PageRequest.class))).thenReturn(List.of(testSong));
        when(userRepository.findByIdWithFavorites(1L)).thenReturn(Optional.of(testUser));

        List<SongDTO> result = songService.getLatestSongs(10, 1L);

        assertEquals(1, result.size());
    }

    @Test
    void getSongsByGenre_ReturnsPage() {
        Page<Song> songPage = new PageImpl<>(List.of(testSong));
        when(songRepository.findByGenre(eq("Pop"), any(Pageable.class))).thenReturn(songPage);
        when(userRepository.findByIdWithFavorites(1L)).thenReturn(Optional.of(testUser));

        Page<SongDTO> result = songService.getSongsByGenre("Pop", PageRequest.of(0, 20), 1L);

        assertEquals(1, result.getContent().size());
        assertEquals("Pop", result.getContent().get(0).getGenre());
    }

    @Test
    void getAllGenres_ReturnsList() {
        when(songRepository.findAllGenres()).thenReturn(List.of("Pop", "Rock", "Jazz"));

        List<String> result = songService.getAllGenres();

        assertEquals(3, result.size());
        assertTrue(result.contains("Pop"));
    }

    @Test
    void getGenreSummaries_ReturnsList() {
        Object[] row = new Object[]{"Pop", 10L};
        List<Object[]> rows = new java.util.ArrayList<>();
        rows.add(row);
        when(songRepository.findGenreCounts()).thenReturn(rows);
        when(songRepository.findTopCoverByGenre("Pop")).thenReturn("http://example.com/cover.jpg");

        List<GenreSummaryDTO> result = songService.getGenreSummaries();

        assertEquals(1, result.size());
        assertEquals("Pop", result.get(0).getGenre());
        assertEquals(10L, result.get(0).getSongCount());
    }

    @Test
    void getRecommendedAlbums_ReturnsPage() {
        Object[] row = new Object[]{"Test Album", "Test Artist", "http://cover.jpg", 5L};
        List<Object[]> rows = new java.util.ArrayList<>();
        rows.add(row);
        when(songRepository.findRecommendedAlbums(any(Pageable.class))).thenReturn(rows);

        Page<AlbumDTO> result = songService.getRecommendedAlbums(PageRequest.of(0, 8));

        assertEquals(1, result.getContent().size());
        assertEquals("Test Album", result.getContent().get(0).getAlbum());
    }

    @Test
    void incrementPlayCount_CallsRepository() {
        doNothing().when(songRepository).incrementPlayCountById(1L);

        songService.incrementPlayCount(1L);

        verify(songRepository).incrementPlayCountById(1L);
    }

    @Test
    void deleteSong_Success() {
        testSong.setUploader(testUser);
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));

        songService.deleteSong(1L, 1L);

        verify(songRepository).delete(testSong);
    }

    @Test
    void deleteSong_NotOwner_ThrowsException() {
        User otherUser = User.builder().id(2L).build();
        testSong.setUploader(otherUser);
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> songService.deleteSong(1L, 1L));
        assertEquals("You can only delete your own songs", ex.getMessage());
    }

    @Test
    void deleteSong_SongNotFound_ThrowsException() {
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> songService.deleteSong(999L, 1L));
    }

    @Test
    void getSongById_WithFavorite() {
        Song favSong = Song.builder().id(1L).build();
        testUser.setFavorites(new HashSet<>(Set.of(favSong)));
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(userRepository.findByIdWithFavorites(1L)).thenReturn(Optional.of(testUser));

        Optional<SongDTO> result = songService.getSongById(1L, 1L);

        assertTrue(result.isPresent());
        assertTrue(result.get().getIsFavorite());
    }
}
