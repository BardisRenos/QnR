package com.example.qnr.security.entities;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "The username cannot be empty or just spaces")
    private String username;

    @NotBlank(message = "The password cannot be empty or just spaces")
    private String password;
}