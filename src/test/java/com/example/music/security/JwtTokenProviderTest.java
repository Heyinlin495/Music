package com.example.music.security;

import com.example.music.entity.Role;
import com.example.music.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret",
                "TestSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong1234567890");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", 86400000L);
    }

    private UserPrincipal createPrincipal() {
        User user = User.builder()
                .id(1L).username("testuser").password("pass")
                .email("test@example.com").nickname("Test User")
                .role(Role.USER).enabled(true).build();
        return UserPrincipal.create(user);
    }

    @Test
    void generateTokenFromUserId() {
        String token = tokenProvider.generateTokenFromUserId(1L);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getUserIdFromToken() {
        String token = tokenProvider.generateTokenFromUserId(42L);

        Long userId = tokenProvider.getUserIdFromToken(token);

        assertEquals(42L, userId);
    }

    @Test
    void validateToken_Valid() {
        String token = tokenProvider.generateTokenFromUserId(1L);

        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void validateToken_Invalid() {
        assertFalse(tokenProvider.validateToken("invalid-token"));
    }

    @Test
    void validateToken_Tampered() {
        String token = tokenProvider.generateTokenFromUserId(1L);
        String tampered = token.substring(0, token.length() - 2) + "XX";

        assertFalse(tokenProvider.validateToken(tampered));
    }

    @Test
    void validateToken_Empty() {
        assertFalse(tokenProvider.validateToken(""));
    }

    @Test
    void generateAndValidate_RoundTrip() {
        String token = tokenProvider.generateTokenFromUserId(123L);

        assertTrue(tokenProvider.validateToken(token));
        assertEquals(123L, tokenProvider.getUserIdFromToken(token));
    }

    @Test
    void generateToken_FromAuthentication() {
        UserPrincipal principal = createPrincipal();
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null);

        String token = tokenProvider.generateToken(auth);

        assertNotNull(token);
        assertEquals(1L, tokenProvider.getUserIdFromToken(token));
    }

    @Test
    void generateTokenFromUserId_DifferentIds() {
        String token1 = tokenProvider.generateTokenFromUserId(1L);
        String token2 = tokenProvider.generateTokenFromUserId(2L);

        assertNotEquals(token1, token2);
        assertEquals(1L, tokenProvider.getUserIdFromToken(token1));
        assertEquals(2L, tokenProvider.getUserIdFromToken(token2));
    }
}
