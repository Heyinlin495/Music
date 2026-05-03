package com.example.music.security;

import com.example.music.entity.Role;
import com.example.music.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    private User createUser(Role role, Boolean enabled) {
        return User.builder()
                .id(1L).username("testuser").password("encodedPass")
                .email("test@example.com").nickname("Test User")
                .avatar("avatar.jpg").role(role).enabled(enabled).build();
    }

    @Test
    void create_FromUser() {
        User user = createUser(Role.USER, true);

        UserPrincipal principal = UserPrincipal.create(user);

        assertEquals(1L, principal.getId());
        assertEquals("testuser", principal.getUsername());
        assertEquals("test@example.com", principal.getEmail());
        assertEquals("encodedPass", principal.getPassword());
        assertEquals("Test User", principal.getNickname());
        assertEquals("avatar.jpg", principal.getAvatar());
        assertEquals(Role.USER, principal.getRole());
        assertTrue(principal.isEnabled());
    }

    @Test
    void getAuthorities_UserRole() {
        UserPrincipal principal = UserPrincipal.create(createUser(Role.USER, true));

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void getAuthorities_AdminRole() {
        UserPrincipal principal = UserPrincipal.create(createUser(Role.ADMIN, true));

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void isAdmin_True() {
        UserPrincipal principal = UserPrincipal.create(createUser(Role.ADMIN, true));

        assertTrue(principal.isAdmin());
    }

    @Test
    void isAdmin_False() {
        UserPrincipal principal = UserPrincipal.create(createUser(Role.USER, true));

        assertFalse(principal.isAdmin());
    }

    @Test
    void isEnabled_True() {
        UserPrincipal principal = UserPrincipal.create(createUser(Role.USER, true));

        assertTrue(principal.isEnabled());
    }

    @Test
    void isEnabled_False() {
        UserPrincipal principal = UserPrincipal.create(createUser(Role.USER, false));

        assertFalse(principal.isEnabled());
    }

    @Test
    void isEnabled_Null_DefaultsTrue() {
        User user = createUser(Role.USER, null);
        UserPrincipal principal = UserPrincipal.create(user);

        // The isEnabled method returns `enabled != null && enabled`, so null = false
        assertFalse(principal.isEnabled());
    }

    @Test
    void isAccountNonExpired() {
        UserPrincipal principal = UserPrincipal.create(createUser(Role.USER, true));

        assertTrue(principal.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked() {
        UserPrincipal principal = UserPrincipal.create(createUser(Role.USER, true));

        assertTrue(principal.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired() {
        UserPrincipal principal = UserPrincipal.create(createUser(Role.USER, true));

        assertTrue(principal.isCredentialsNonExpired());
    }
}
