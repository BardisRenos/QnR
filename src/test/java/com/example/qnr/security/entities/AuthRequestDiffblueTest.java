package com.example.qnr.security.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthRequestDiffblueTest {

    @Test
    void testGettersAndSetters() {
        AuthRequest actualAuthRequest = new AuthRequest("janedoe", "iloveyou");
        actualAuthRequest.setPassword("iloveyou");
        actualAuthRequest.setUsername("janedoe");
        String actualToStringResult = actualAuthRequest.toString();
        String actualPassword = actualAuthRequest.getPassword();

        assertEquals("AuthRequest(username=janedoe, password=iloveyou)", actualToStringResult);
        assertEquals("iloveyou", actualPassword);
        assertEquals("janedoe", actualAuthRequest.getUsername());
    }
}
