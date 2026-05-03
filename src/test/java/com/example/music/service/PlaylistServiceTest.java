package com.example.music.service;

import com.example.music.dto.PlaylistDTO;
import com.example.music.entity.Playlist;
import com.example.music.entity.Song;
import com.example.music.entity.User;
import com.example.music.entity.Role;
import com.example.music.repository.PlaylistRepository;
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
class PlaylistServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private SongRepository songRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PlaylistService playlistService;

    private User testUser;
    private Playlist testPlaylist;
    private Song testSong;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).username("testuser").nickname("Test User").role(Role.USER).build();
        testSong = Song.builder()
                .id(1L).title("Test Song").artist("Test Artist").playCount(0L).build();
        testPlaylist = Playlist.builder()
                .id(1L).name("Test Playlist").description("Description").isPublic(true)
                .user(testUser).songs(new ArrayList<>(List.of(testSong))).build();
    }

    @Test
    void getUserPlaylists_ReturnsList() {
        when(playlistRepository.findByUserIdWithSongs(1L)).thenReturn(List.of(testPlaylist));

        List<PlaylistDTO> result = playlistService.getUserPlaylists(1L);

        assertEquals(1, result.size());
        assertEquals("Test Playlist", result.get(0).getName());
    }

    @Test
    void getPublicPlaylists_ReturnsPage() {
        Page<Playlist> page = new PageImpl<>(List.of(testPlaylist));
        when(playlistRepository.findByIsPublicTrue(any(Pageable.class))).thenReturn(page);

        Page<PlaylistDTO> result = playlistService.getPublicPlaylists(PageRequest.of(0, 20));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getFeaturedPlaylists_ReturnsList() {
        when(playlistRepository.findFeaturedPlaylists(any(PageRequest.class))).thenReturn(List.of(testPlaylist));

        List<PlaylistDTO> result = playlistService.getFeaturedPlaylists(6);

        assertEquals(1, result.size());
    }

    @Test
    void getPlaylistById_PublicPlaylist() {
        when(playlistRepository.findByIdWithSongs(1L)).thenReturn(Optional.of(testPlaylist));

        PlaylistDTO result = playlistService.getPlaylistById(1L, null);

        assertNotNull(result);
        assertEquals("Test Playlist", result.getName());
        assertEquals(1, result.getSongCount());
    }

    @Test
    void getPlaylistById_PrivatePlaylist_OwnerAccess() {
        testPlaylist.setIsPublic(false);
        when(playlistRepository.findByIdWithSongs(1L)).thenReturn(Optional.of(testPlaylist));

        PlaylistDTO result = playlistService.getPlaylistById(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void getPlaylistById_PrivatePlaylist_NonOwner_ThrowsException() {
        testPlaylist.setIsPublic(false);
        when(playlistRepository.findByIdWithSongs(1L)).thenReturn(Optional.of(testPlaylist));

        assertThrows(RuntimeException.class, () -> playlistService.getPlaylistById(1L, 2L));
    }

    @Test
    void getPlaylistById_NotFound_ThrowsException() {
        when(playlistRepository.findByIdWithSongs(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> playlistService.getPlaylistById(999L, null));
    }

    @Test
    void createPlaylist_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> {
            Playlist p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });

        PlaylistDTO result = playlistService.createPlaylist("New Playlist", "Description", true, 1L);

        assertNotNull(result);
        assertEquals("New Playlist", result.getName());
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    void createPlaylist_NullIsPublic_DefaultsTrue() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> {
            Playlist p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });

        PlaylistDTO result = playlistService.createPlaylist("New Playlist", "Description", null, 1L);

        assertTrue(result.getIsPublic());
    }

    @Test
    void createPlaylist_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> playlistService.createPlaylist("New", "Desc", true, 999L));
    }

    @Test
    void updatePlaylist_Success() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(testPlaylist);

        PlaylistDTO result = playlistService.updatePlaylist(1L, "Updated", "New Desc", false, 1L);

        assertNotNull(result);
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    void updatePlaylist_NotOwner_ThrowsException() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));

        assertThrows(RuntimeException.class,
                () -> playlistService.updatePlaylist(1L, "Updated", null, null, 2L));
    }

    @Test
    void deletePlaylist_Success() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));

        playlistService.deletePlaylist(1L, 1L);

        verify(playlistRepository).delete(testPlaylist);
    }

    @Test
    void deletePlaylist_NotOwner_ThrowsException() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));

        assertThrows(RuntimeException.class, () -> playlistService.deletePlaylist(1L, 2L));
    }

    @Test
    void addSongToPlaylist_Success() {
        when(playlistRepository.findByIdWithSongs(1L)).thenReturn(Optional.of(testPlaylist));
        when(songRepository.findById(2L)).thenReturn(Optional.of(Song.builder().id(2L).title("New Song").build()));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(testPlaylist);

        PlaylistDTO result = playlistService.addSongToPlaylist(1L, 2L, 1L);

        assertNotNull(result);
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    void addSongToPlaylist_AlreadyInPlaylist_NoDuplicate() {
        when(playlistRepository.findByIdWithSongs(1L)).thenReturn(Optional.of(testPlaylist));
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));

        PlaylistDTO result = playlistService.addSongToPlaylist(1L, 1L, 1L);

        assertNotNull(result);
        // Song already exists, so save should not be called again
        verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    void addSongToPlaylist_NotOwner_ThrowsException() {
        when(playlistRepository.findByIdWithSongs(1L)).thenReturn(Optional.of(testPlaylist));

        assertThrows(RuntimeException.class,
                () -> playlistService.addSongToPlaylist(1L, 2L, 2L));
    }

    @Test
    void removeSongFromPlaylist_Success() {
        when(playlistRepository.findByIdWithSongs(1L)).thenReturn(Optional.of(testPlaylist));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(testPlaylist);

        PlaylistDTO result = playlistService.removeSongFromPlaylist(1L, 1L, 1L);

        assertNotNull(result);
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    void removeSongFromPlaylist_NotOwner_ThrowsException() {
        when(playlistRepository.findByIdWithSongs(1L)).thenReturn(Optional.of(testPlaylist));

        assertThrows(RuntimeException.class,
                () -> playlistService.removeSongFromPlaylist(1L, 1L, 2L));
    }
}
