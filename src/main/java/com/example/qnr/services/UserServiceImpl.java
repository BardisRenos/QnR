package com.example.qnr.services;

import com.example.qnr.dao.UserRepository;
import com.example.qnr.dto.UserDto;
import com.example.qnr.exception.NotFoundException;
import com.example.qnr.mappers.UserMapper;
import com.example.qnr.security.JwtUtil;
import com.example.qnr.security.entities.AuthRequest;
import com.example.qnr.security.entities.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = {"users"})
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toOrderDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "orders", key = "#role")
    public List<UserDto> getByUserRole(String role) throws NotFoundException {
        return userRepository.findByRole(role)
                .orElseThrow(()-> new NotFoundException("No user found with role: "+ role))
                .stream()
                .map(UserMapper::toOrderDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto insertUser(UserDto userDto) {
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return UserMapper.toOrderDto(userRepository.save(UserMapper.toOrders(userDto)));
    }

    @Override
    public AuthResponse verify(AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

            return new AuthResponse(jwtUtil.generateToken(authRequest.getUsername()));
        } catch (BadCredentialsException e) {
            return new AuthResponse("Authentication failed: Invalid username or password");
        } catch (Exception e) {
            return new AuthResponse("Authentication failed: An unexpected error occurred");
        }
    }

}
