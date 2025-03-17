package com.example.qnr.controllers;

import com.example.qnr.dto.UserDto;
import com.example.qnr.dto.UserDtoNoPass;
import com.example.qnr.exception.NotFoundException;
import com.example.qnr.security.entities.AuthRequest;
import com.example.qnr.security.entities.AuthResponse;
import com.example.qnr.services.CustomUserDetailsService;
import com.example.qnr.services.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1.0/user")
public class UserController {

    private final UserServiceImpl userService;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping("/all")
    public ResponseEntity<List<UserDtoNoPass>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @GetMapping("/{user_role}")
    public ResponseEntity<List<UserDtoNoPass>> getUsersByRole(@PathVariable("user_role") String role) throws NotFoundException {
        return new ResponseEntity<>(userService.getByUserRole(role), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDtoNoPass> addUser(@RequestBody UserDto userDto) {
        return new ResponseEntity<>(userService.insertUser(userDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest authRequest) {
        return userService.verify(authRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token");
        }

        String token = authHeader.substring(7);
        userDetailsService.blacklistToken(token);

        return ResponseEntity.ok("Logged out successfully");
    }
}