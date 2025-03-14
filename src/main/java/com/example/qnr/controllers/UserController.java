package com.example.qnr.controllers;

import com.example.qnr.dto.UserDto;
import com.example.qnr.exception.NotFoundException;
import com.example.qnr.security.entities.AuthRequest;
import com.example.qnr.security.entities.AuthResponse;
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

    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @GetMapping("/{user_role}")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable("user_role") String role) throws NotFoundException {
        return new ResponseEntity<>(userService.getByUserRole(role), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> addUser(@RequestBody UserDto userDto) {
        return new ResponseEntity<>(userService.insertUser(userDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest authRequest) {
        return userService.verify(authRequest);
    }
}
