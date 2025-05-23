package com.example.qnr.security.auth;

import com.example.qnr.resources.Users;
import com.example.qnr.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPrincipalTest {

    @Mock
    private Users mockUser;

    private UserPrincipal userPrincipalUnderTest;

    @BeforeEach
    void setUp() {
        userPrincipalUnderTest = new UserPrincipal(mockUser);
    }

    @Test
    void getAuthorities_ShouldReturnAuthorities_WhenCalled() {
        when(mockUser.getRole()).thenReturn("USER");

        Collection<? extends GrantedAuthority> authorities = userPrincipalUnderTest.getAuthorities();
        authorities.forEach(authority -> System.out.println("Authority: " + authority.getAuthority()));

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void getPassword_ShouldReturnPassword_WhenCalled() {
        when(mockUser.getPassword()).thenReturn("password");
        final String result = userPrincipalUnderTest.getPassword();
        assertThat(result).isEqualTo("password");
    }

    @Test
    void getUsername_ShouldReturnUsername_WhenCalled() {
        when(mockUser.getUsername()).thenReturn("username");
        final String result = userPrincipalUnderTest.getUsername();
        assertThat(result).isEqualTo("username");
    }
}
