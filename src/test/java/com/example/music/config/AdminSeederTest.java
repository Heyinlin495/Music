package com.example.music.config;

import com.example.music.entity.Role;
import com.example.music.entity.User;
import com.example.music.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminSeeder adminSeeder;

    @BeforeEach
    void setUp() {
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");
    }

    @Test
    void createsAdminWhenNoneExists() {
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(0L);

        adminSeeder.run();

        verify(userRepository, times(1)).countByRole(Role.ADMIN);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("admin", saved.getUsername());
        assertEquals("admin@music.local", saved.getEmail());
        assertEquals(Role.ADMIN, saved.getRole());
        assertTrue(saved.getEnabled());
        assertNotNull(saved.getPassword());
    }

    @Test
    void skipsWhenAdminExists() {
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(2L);

        adminSeeder.run();

        verify(userRepository, never()).save(any());
    }
}
