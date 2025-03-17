package com.example.qnr.services;

import com.example.qnr.dao.UserRepository;
import com.example.qnr.resources.Users;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_ShouldReturnUserPrincipal_WhenUserExists() {
        String username = "john_doe";
        Users user = new Users(1, "john_doe", "encodedPassword", "USER");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        var result = customUserDetailsService.loadUserByUsername(username);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(user.getUsername());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserDoesNotExist() {
        String username = "unknown_user";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
        verify(userRepository).findByUsername(username);
    }

    @Test
    void testBlacklistToken() {
        String token = "sampleToken123";

        assertFalse(customUserDetailsService.isTokenBlacklisted(token));

        customUserDetailsService.blacklistToken(token);

        assertTrue(customUserDetailsService.isTokenBlacklisted(token));
    }

    @Test
    void testIsTokenBlacklisted() {
        String token1 = "blacklistedToken";
        String token2 = "nonBlacklistedToken";

        customUserDetailsService.blacklistToken(token1);

        assertTrue(customUserDetailsService.isTokenBlacklisted(token1));
        assertFalse(customUserDetailsService.isTokenBlacklisted(token2));
    }
}