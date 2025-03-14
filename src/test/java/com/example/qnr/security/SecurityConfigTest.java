package com.example.qnr.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        userDetailsService = mock(UserDetailsService.class);
        jwtFilter = mock(JwtFilter.class);
        securityConfig = new SecurityConfig(userDetailsService, jwtFilter);
    }

    @Test
    void testSecurityFilterChain() throws Exception {
        HttpSecurity httpSecurity = mock(HttpSecurity.class);
        SecurityFilterChain securityFilterChain = mock(SecurityFilterChain.class);
        doReturn(httpSecurity).when(httpSecurity).csrf(any());
        doReturn(httpSecurity).when(httpSecurity).authorizeHttpRequests(any());
        doReturn(httpSecurity).when(httpSecurity).httpBasic(any());
        doReturn(httpSecurity).when(httpSecurity).sessionManagement(any());
        doReturn(httpSecurity).when(httpSecurity).addFilterBefore(eq(jwtFilter), eq(UsernamePasswordAuthenticationFilter.class));
        doReturn(securityFilterChain).when(httpSecurity).build();
        SecurityFilterChain result = securityConfig.securityFilterChain(httpSecurity);
        assertThat(result, is(instanceOf(SecurityFilterChain.class)));
        verify(httpSecurity, times(1)).csrf(any());
        verify(httpSecurity, times(1)).authorizeHttpRequests(any());
        verify(httpSecurity, times(1)).httpBasic(any());
        verify(httpSecurity, times(1)).sessionManagement(any());
        verify(httpSecurity, times(1)).addFilterBefore(eq(jwtFilter), eq(UsernamePasswordAuthenticationFilter.class));
        verify(httpSecurity, times(1)).build();
    }

    @Test
    void testAuthenticationManager() throws Exception {
        AuthenticationConfiguration config = mock(AuthenticationConfiguration.class);
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        doReturn(authManager).when(config).getAuthenticationManager();
        AuthenticationManager result = securityConfig.authenticationManager(config);
        assertThat(result, is(authManager));
        verify(config, times(1)).getAuthenticationManager();
    }

    @Test
    void testPasswordEncoder() {
        PasswordEncoder result = securityConfig.passwordEncoder();
        assertThat(result, is(instanceOf(BCryptPasswordEncoder.class)));
    }
}