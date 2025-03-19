package com.example.qnr.security;

import com.example.qnr.resources.Users;
import com.example.qnr.services.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private SecurityProperties mockSecurityProperties;

    @Mock
    private CustomUserDetailsService mockUserDetailsService;

    @InjectMocks
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        when(mockSecurityProperties.getExpiration()).thenReturn(1000L * 60 * 60);
    }

    @Test
    void generateToken_ShouldReturnNonBlankToken_WhenCalledWithValidUsername() {
        Users mockUser = new Users(1, "username", "ADMIN", "password");
        UserDetails mockUserDetails = new UserPrincipal(mockUser);

        when(mockUserDetailsService.loadUserByUsername("username")).thenReturn(mockUserDetails);

        final String result = jwtUtil.generateToken("username");

        assertThat(result).isNotBlank();
        assertThat(3).isEqualTo(result.split("\\.").length);
    }

    @Test
    void extractUsername_ShouldReturnUsername_WhenTokenIsValid() {
        Users mockUser = new Users(1, "testuser", "USER", "password");
        UserDetails mockUserDetails = new UserPrincipal(mockUser);

        when(mockUserDetailsService.loadUserByUsername("testuser")).thenReturn(mockUserDetails);

        String token = jwtUtil.generateToken("testuser");
        String username = jwtUtil.extractUsername(token);

        assertEquals("testuser", username);
    }


    @Test
    void extractClaim_ShouldReturnExpirationDate_WhenTokenIsValid() {
        Users mockUser = new Users(1, "testuser", "USER", "password");
        UserDetails mockUserDetails = new UserPrincipal(mockUser);

        when(mockUserDetailsService.loadUserByUsername("testuser")).thenReturn(mockUserDetails);

        String token = jwtUtil.generateToken("testuser");
        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }


    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValidAndUserDetailsMatch() {
        Users mockUser = new Users(1, "john", "ADMIN", "pass123");
        UserDetails mockUserDetails = new UserPrincipal(mockUser);
        when(mockUserDetailsService.loadUserByUsername("john")).thenReturn(mockUserDetails);

        String token = jwtUtil.generateToken("john");

        assertTrue(jwtUtil.validateToken(token, mockUserDetails));
    }
}