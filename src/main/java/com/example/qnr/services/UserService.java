package com.example.qnr.services;

import com.example.qnr.dto.UserDto;
import com.example.qnr.exception.NotFoundException;
import com.example.qnr.security.entities.AuthRequest;
import com.example.qnr.security.entities.AuthResponse;

import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();
    List<UserDto> getByUserRole(String role) throws NotFoundException;
    UserDto insertUser(UserDto userDto);
    AuthResponse verify(AuthRequest authRequest);
}
