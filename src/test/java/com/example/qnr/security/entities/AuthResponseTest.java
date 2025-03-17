package com.example.qnr.security.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthResponseTest {

    private AuthResponse authResponseUnderTest;

    @BeforeEach
    void setUp() {
        authResponseUnderTest = new AuthResponse("token");
    }

    @Test
    void testTokenGetterAndSetter() {
        final String token = "token";
        assertThat(authResponseUnderTest.getToken()).isEqualTo(token);
    }
}
