package com.example.qnr.security;

import com.example.qnr.resources.Users;
import com.example.qnr.security.entities.UserPrincipal;
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

    @InjectMocks
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        when(mockSecurityProperties.getExpiration()).thenReturn(1000L * 60 * 60);
        jwtUtil = new JwtUtil(mockSecurityProperties);
    }

    @Test
    void testGenerateToken() {
        when(mockSecurityProperties.getExpiration()).thenReturn(0L);
        final String result = jwtUtil.generateToken("username");
        assertThat(result).isNotBlank();
        assertThat(3).isEqualTo(result.split("\\.").length);
    }

    @Test
    void testExtractUsername() {
        String token = jwtUtil.generateToken("testuser");
        String username = jwtUtil.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void testExtractClaim() {
        String token = jwtUtil.generateToken("testuser");
        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testValidateToken() {
        String token = jwtUtil.generateToken("john");
        Users user = new Users(1, "john", "ADMIN",  "pass123");
        UserDetails userDetails = new UserPrincipal(user);
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }


}
