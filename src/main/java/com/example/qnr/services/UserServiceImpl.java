package com.example.qnr.services;

import com.example.qnr.dao.UserRepository;
import com.example.qnr.dto.UserDto;
import com.example.qnr.dto.UserDtoNoPass;
import com.example.qnr.exception.NotFoundException;
import com.example.qnr.mappers.UserMapper;
import com.example.qnr.security.JwtUtil;
import com.example.qnr.security.auth.AuthRequest;
import com.example.qnr.security.auth.AuthResponse;
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

/**
 * Service implementation for managing users.
 * Provides methods for user registration, login authentication, and retrieving user details by role.
 * Utilizes caching to optimize user retrieval based on role.
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = {"users"})
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    /**
     * Retrieves all users without passwords.
     *
     * @return a list of UserDtoNoPass, representing all users in the system without sensitive information (password).
     */
    @Override
    public List<UserDtoNoPass> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserDtoNoPass)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves users based on their role from the cache or database.
     * The result is cached to optimize subsequent requests for the same role.
     *
     * @param role the role of the users to retrieve (e.g., "admin", "user").
     * @return a list of UserDtoNoPass representing users with the given role.
     * @throws NotFoundException if no users are found with the given role.
     */
    @Override
    @Cacheable(value = "users", key = "#role")
    public List<UserDtoNoPass> getByUserRole(String role) throws NotFoundException {
        return userRepository.findByRole(role)
                .orElseThrow(() -> new NotFoundException("No user found with role: " + role))
                .stream()
                .map(userMapper::toUserDtoNoPass)
                .collect(Collectors.toList());
    }

    /**
     * Registers a new user in the system.
     * The password is encoded using BCrypt before saving the user to the database.
     *
     * @param userDto the UserDto object containing the user's registration details.
     * @return the UserDto representing the newly registered user.
     */
    @Override
    public UserDto insertUser(UserDto userDto) {
        // Encode the password before saving the user
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return userMapper.toUserDto(userRepository.save(userMapper.toUsers(userDto)));
    }

    /**
     * Authenticates a user based on the provided credentials.
     * If successful, a JWT token is generated; otherwise, an authentication failure message is returned.
     *
     * @param authRequest the AuthRequest object containing the username and password for authentication.
     * @return an AuthResponse containing the JWT token if authentication is successful, or an error message if authentication fails.
     */
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
