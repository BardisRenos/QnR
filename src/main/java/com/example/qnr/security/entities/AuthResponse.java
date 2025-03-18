package com.example.qnr.security.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents an authentication response containing the JWT token.
 * This class is used to return the generated token to the user upon successful authentication.
 */
@Data
@AllArgsConstructor
public class AuthResponse {

    /**
     * The JWT token generated after successful authentication.
     * This token is used for subsequent requests to authenticate the user.
     */
    private String token;
}