package com.example.qnr.controllers;

import com.example.qnr.dto.UserDto;
import com.example.qnr.dto.UserDtoNoPass;
import com.example.qnr.exception.NotFoundException;
import com.example.qnr.security.SecurityProperties;
import com.example.qnr.security.auth.AuthRequest;
import com.example.qnr.security.auth.AuthResponse;
import com.example.qnr.services.CustomUserDetailsService;
import com.example.qnr.services.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing user-related operations.
 * This class exposes various endpoints to handle user registration, login, logout, and fetching user details.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1.0/user")
public class UserController {

    private final UserServiceImpl userService;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityProperties securityProperties;

    /**
     * Retrieves all users without passwords.
     *
     * @return a ResponseEntity containing a list of all users (without passwords).
     */
    @GetMapping("/all")
    public ResponseEntity<List<UserDtoNoPass>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    /**
     * Retrieves users by their role.
     *
     * @param role the role of the users to retrieve (e.g., "admin", "user").
     * @return a ResponseEntity containing a list of users that match the provided role.
     * @throws NotFoundException if no users are found with the given role.
     */
    @GetMapping("/{user_role}")
    public ResponseEntity<List<UserDtoNoPass>> getUsersByRole(@PathVariable("user_role") String role) throws NotFoundException {
        return new ResponseEntity<>(userService.getByUserRole(role), HttpStatus.OK);
    }

    /**
     * Registers a new user.
     *
     * @param userDto the UserDto object containing the user's registration information.
     * @return a ResponseEntity containing the newly registered UserDto (without password).
     */
    @PostMapping("/register")
    public ResponseEntity<UserDtoNoPass> addUser(@RequestBody UserDto userDto) {
        return new ResponseEntity<>(userService.insertUser(userDto), HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param authRequest the AuthRequest object containing the username and password.
     * @return an AuthResponse containing the JWT token if authentication is successful.
     */
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest authRequest) {
        return userService.verify(authRequest);
    }

    /**
     * Logs out a user by invalidating their JWT token.
     *
     * @param authHeader the authorization header containing the bearer token.
     * @return a ResponseEntity with a success message if the logout is successful, or an error message if the token is invalid.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith(securityProperties.getTokenPrefix())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token");
        }

        String token = authHeader.substring(7);
        userDetailsService.blacklistToken(token);

        return ResponseEntity.ok("Logged out successfully");
    }
}