package com.example.music.service;

import com.example.music.dto.AuthDTO;
import com.example.music.entity.Role;
import com.example.music.entity.User;
import com.example.music.repository.UserRepository;
import com.example.music.security.JwtTokenProvider;
import com.example.music.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private Authentication authentication;
    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserPrincipal testUserPrincipal;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .email("test@example.com")
                .nickname("Test")
                .role(Role.USER)
                .enabled(true)
                .build();
        testUserPrincipal = UserPrincipal.create(testUser);
    }

    @Test
    void login_Success() {
        AuthDTO.LoginRequest request = new AuthDTO.LoginRequest("testuser", "password", "id", "code");
        when(captchaService.verifyCaptcha("id", "code")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUserPrincipal);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");

        AuthDTO.AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("Test", response.getNickname());
    }

    @Test
    void login_BadCredentials() {
        AuthDTO.LoginRequest request = new AuthDTO.LoginRequest("testuser", "wrongpassword", "id", "code");
        when(captchaService.verifyCaptcha("id", "code")).thenReturn(true);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        assertThrows(org.springframework.security.authentication.BadCredentialsException.class,
                () -> authService.login(request));
    }

    @Test
    void register_Success() {
        AuthDTO.RegisterRequest request = new AuthDTO.RegisterRequest("newuser", "password", "new@example.com", "New");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(tokenProvider.generateTokenFromUserId(2L)).thenReturn("jwt-token");

        AuthDTO.AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(2L, response.getUserId());
        assertEquals("newuser", response.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UsernameTaken() {
        AuthDTO.RegisterRequest request = new AuthDTO.RegisterRequest("existinguser", "password", "new@example.com", "New User");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertEquals("Username is already taken", ex.getMessage());
    }

    @Test
    void register_EmailTaken() {
        AuthDTO.RegisterRequest request = new AuthDTO.RegisterRequest("newuser", "password", "existing@example.com", "New User");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertEquals("Email is already in use", ex.getMessage());
    }

    @Test
    void register_NullNickname_UsesUsername() {
        AuthDTO.RegisterRequest request = new AuthDTO.RegisterRequest("newuser", "password", "new@example.com", null);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(tokenProvider.generateTokenFromUserId(2L)).thenReturn("jwt-token");

        AuthDTO.AuthResponse response = authService.register(request);

        assertEquals("newuser", response.getNickname());
    }
}
