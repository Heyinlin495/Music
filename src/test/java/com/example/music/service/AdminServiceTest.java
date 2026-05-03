package com.example.music.service;

import com.example.music.dto.SongDTO;
import com.example.music.entity.Role;
import com.example.music.entity.Song;
import com.example.music.entity.User;
import com.example.music.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SongRepository songRepository;
    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PlayHistoryRepository playHistoryRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private User adminUser;
    private User normalUser;
    private Song testSong;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L).username("admin").nickname("Admin").role(Role.ADMIN).enabled(true).build();
        normalUser = User.builder()
                .id(2L).username("user1").nickname("User 1").role(Role.USER).enabled(true).build();
        testSong = Song.builder()
                .id(1L).title("Test Song").artist("Artist").genre("Pop").build();
    }

    @Test
    void getDashboardStats_ReturnsMap() {
        when(userRepository.count()).thenReturn(10L);
        when(songRepository.count()).thenReturn(50L);
        when(playlistRepository.count()).thenReturn(20L);
        when(commentRepository.count()).thenReturn(100L);
        when(userRepository.countActiveUsers()).thenReturn(8L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(2L);
        when(songRepository.sumAllPlayCounts()).thenReturn(5000L);

        Map<String, Object> stats = adminService.getDashboardStats();

        assertEquals(10L, stats.get("totalUsers"));
        assertEquals(50L, stats.get("totalSongs"));
        assertEquals(20L, stats.get("totalPlaylists"));
        assertEquals(100L, stats.get("totalComments"));
        assertEquals(8L, stats.get("activeUsers"));
        assertEquals(2L, stats.get("adminCount"));
        assertEquals(5000L, stats.get("totalPlays"));
    }

    @Test
    void getAllUsers_ReturnsPage() {
        Page<User> page = new PageImpl<>(List.of(adminUser, normalUser));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<User> result = adminService.getAllUsers(PageRequest.of(0, 10));

        assertEquals(2, result.getContent().size());
    }

    @Test
    void searchUsers_ReturnsPage() {
        Page<User> page = new PageImpl<>(List.of(normalUser));
        when(userRepository.findByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCase(
                eq("user"), eq("user"), any(Pageable.class))).thenReturn(page);

        Page<User> result = adminService.searchUsers("user", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        User result = adminService.getUserById(1L);

        assertEquals("admin", result.getUsername());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminService.getUserById(999L));
    }

    @Test
    void updateUserRole_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));
        when(userRepository.save(any(User.class))).thenReturn(normalUser);

        User result = adminService.updateUserRole(2L, Role.ADMIN);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void toggleUserStatus_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));
        when(userRepository.save(any(User.class))).thenReturn(normalUser);

        User result = adminService.toggleUserStatus(2L);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));

        adminService.deleteUser(2L);

        verify(userRepository).delete(normalUser);
    }

    @Test
    void deleteUser_LastAdmin_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminService.deleteUser(1L));
        assertEquals("Cannot delete the last admin", ex.getMessage());
    }

    @Test
    void deleteUser_AdminWithMultipleAdmins_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(3L);

        adminService.deleteUser(1L);

        verify(userRepository).delete(adminUser);
    }

    @Test
    void createAdmin_Success() {
        when(userRepository.existsByUsername("newadmin")).thenReturn(false);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(3L);
            return u;
        });

        User result = adminService.createAdmin("newadmin", "password", "admin@example.com", "New Admin");

        assertNotNull(result);
        assertEquals(Role.ADMIN, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createAdmin_UsernameExists_ThrowsException() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> adminService.createAdmin("existing", "password", "email@example.com", "Name"));
    }

    @Test
    void createAdmin_EmailExists_ThrowsException() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> adminService.createAdmin("newuser", "password", "existing@example.com", "Name"));
    }

    @Test
    void getAllSongs_ReturnsPage() {
        Page<Song> page = new PageImpl<>(List.of(testSong));
        when(songRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<SongDTO> result = adminService.getAllSongs(PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchSongs_ReturnsPage() {
        Page<Song> page = new PageImpl<>(List.of(testSong));
        when(songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
                eq("Test"), eq("Test"), any(Pageable.class))).thenReturn(page);

        Page<SongDTO> result = adminService.searchSongs("Test", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void deleteSong_Success() {
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));

        adminService.deleteSong(1L);

        verify(songRepository).delete(testSong);
    }

    @Test
    void deleteSong_NotFound_ThrowsException() {
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminService.deleteSong(999L));
    }

    @Test
    void updateSong_Success() {
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(songRepository.save(any(Song.class))).thenReturn(testSong);

        Song result = adminService.updateSong(1L, "New Title", "New Artist", "New Album", "Rock");

        assertNotNull(result);
        verify(songRepository).save(any(Song.class));
    }

    @Test
    void updateSong_NullFields_NoChange() {
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(songRepository.save(any(Song.class))).thenReturn(testSong);

        Song result = adminService.updateSong(1L, null, null, null, null);

        assertNotNull(result);
    }

    @Test
    void updateSong_NotFound_ThrowsException() {
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> adminService.updateSong(999L, "Title", null, null, null));
    }
}
