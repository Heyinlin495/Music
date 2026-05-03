package com.example.music.service;

import com.example.music.dto.SongDTO;
import com.example.music.dto.UserDTO;
import com.example.music.entity.Role;
import com.example.music.entity.Song;
import com.example.music.entity.User;
import com.example.music.repository.SongRepository;
import com.example.music.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SongRepository songRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Song testSong;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).username("testuser").email("test@example.com")
                .nickname("Test User").bio("Bio").avatar("avatar.jpg")
                .password("encodedPass").role(Role.USER).enabled(true)
                .favorites(new HashSet<>()).build();
        testSong = Song.builder()
                .id(1L).title("Test Song").artist("Artist").playCount(0L).build();
    }

    @Test
    void getUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<UserDTO> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<UserDTO> result = userService.getUserById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void getUserByUsername_Found() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<UserDTO> result = userService.getUserByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO result = userService.updateUser(1L, "New", "New Bio", "newavatar.jpg", null);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_NullFields_NoChange() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO result = userService.updateUser(1L, null, null, null, null);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_NotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.updateUser(999L, "Nick", null, null, null));
    }

    @Test
    void changePassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", "encodedPass")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newEncodedPass");

        userService.changePassword(1L, "oldPass", "newPass");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_WrongOldPassword_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.changePassword(1L, "wrongPass", "newPass"));
        assertEquals("Old password is incorrect", ex.getMessage());
    }

    @Test
    void addFavorite_Success() {
        when(userRepository.findByIdWithFavorites(1L)).thenReturn(Optional.of(testUser));
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));

        userService.addFavorite(1L, 1L);

        assertTrue(testUser.getFavorites().contains(testSong));
        verify(userRepository).save(testUser);
    }

    @Test
    void addFavorite_UserNotFound_ThrowsException() {
        when(userRepository.findByIdWithFavorites(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.addFavorite(999L, 1L));
    }

    @Test
    void addFavorite_SongNotFound_ThrowsException() {
        when(userRepository.findByIdWithFavorites(1L)).thenReturn(Optional.of(testUser));
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.addFavorite(1L, 999L));
    }

    @Test
    void removeFavorite_Success() {
        testUser.getFavorites().add(testSong);
        when(userRepository.findByIdWithFavorites(1L)).thenReturn(Optional.of(testUser));

        userService.removeFavorite(1L, 1L);

        assertFalse(testUser.getFavorites().contains(testSong));
        verify(userRepository).save(testUser);
    }

    @Test
    void getFavorites_ReturnsList() {
        testUser.getFavorites().add(testSong);
        when(userRepository.findByIdWithFavorites(1L)).thenReturn(Optional.of(testUser));

        List<SongDTO> result = userService.getFavorites(1L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsFavorite());
    }

    @Test
    void isFavorite_ReturnsTrue() {
        when(userRepository.isFavorite(1L, 1L)).thenReturn(true);

        boolean result = userService.isFavorite(1L, 1L);

        assertTrue(result);
    }

    @Test
    void isFavorite_ReturnsFalse() {
        when(userRepository.isFavorite(1L, 1L)).thenReturn(false);

        boolean result = userService.isFavorite(1L, 1L);

        assertFalse(result);
    }
}
