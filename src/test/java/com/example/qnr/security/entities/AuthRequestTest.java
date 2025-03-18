package com.example.qnr.security.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthRequestTest {

    @Test
    void gettersAndSetters_ShouldWorkAsExpected_WhenCalled() {
        AuthRequest actualAuthRequest = new AuthRequest("janedoe", "pass_1233");
        actualAuthRequest.setPassword("pass_1233");
        actualAuthRequest.setUsername("janedoe");
        String actualToStringResult = actualAuthRequest.toString();
        String actualPassword = actualAuthRequest.getPassword();

        assertEquals("AuthRequest(username=janedoe, password=pass_1233)", actualToStringResult);
        assertEquals("pass_1233", actualPassword);
        assertEquals("janedoe", actualAuthRequest.getUsername());
    }
}
