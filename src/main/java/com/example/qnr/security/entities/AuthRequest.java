package com.example.qnr.security.entities;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * Represents an authentication request containing the username and password.
 * This class is used to capture the user's login credentials when attempting to authenticate.
 */
@Data
@AllArgsConstructor
public class AuthRequest {

    /**
     * The username of the user attempting to authenticate.
     * It cannot be null, empty, or consist of only spaces.
     */
    @NotBlank(message = "The username cannot be empty or just spaces")
    private String username;

    /**
     * The password of the user attempting to authenticate.
     * It cannot be null, empty, or consist of only spaces.
     */
    @NotBlank(message = "The password cannot be empty or just spaces")
    private String password;
}