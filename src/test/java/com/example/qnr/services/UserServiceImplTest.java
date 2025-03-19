package com.example.qnr.services;

import com.example.qnr.dao.UserRepository;
import com.example.qnr.dto.UserDto;
import com.example.qnr.dto.UserDtoNoPass;
import com.example.qnr.exception.NotFoundException;
import com.example.qnr.mappers.UserMapper;
import com.example.qnr.resources.Users;
import com.example.qnr.resources.enums.UserRole;
import com.example.qnr.security.JwtUtil;
import com.example.qnr.security.auth.AuthRequest;
import com.example.qnr.security.auth.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private AuthRequest authRequest;
    public UserDto inputUserDto;
    public Users savedUser;
    public UserDto returnedUserDto;

    @BeforeEach
    public void setup() {
        inputUserDto = new UserDto("john_doe", UserRole.USER, "plainPassword");
        savedUser = new Users(1, "john_doe", UserRole.USER.toString(), "encodedPassword");
        returnedUserDto = new UserDto("john_doe", UserRole.USER, "encodedPassword");
        authRequest = new AuthRequest("john_doe", "password123");
        lenient().when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        lenient().when(userRepository.save(any())).thenReturn(savedUser);
    }

    @Test
    public void testGetAllUsers() {
        List<Users> mockUsers = List.of(
                new Users(1, "john_doe", UserRole.ADMIN.toString(), "password123"),
                new Users(2, "jane_smith", UserRole.USER.toString(), "securepass"),
                new Users(3, "guest_user", UserRole.MANAGER.toString(), "guestpass")
        );

        when(userRepository.findAll()).thenReturn(mockUsers);

        when(userMapper.toUserDtoNoPass(any(Users.class))).thenAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            return new UserDtoNoPass(user.getUsername(), UserRole.valueOf(user.getRole()));
        });
        List<UserDtoNoPass> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(3, users.size());
        assertEquals("john_doe", users.get(0).getUsername());
        assertEquals("jane_smith", users.get(1).getUsername());
        assertEquals("guest_user", users.get(2).getUsername());
        assertEquals(UserRole.ADMIN, users.get(0).getRole());
        assertEquals(UserRole.USER, users.get(1).getRole());

        verify(userRepository, times(1)).findAll();

        verify(userMapper, times(3)).toUserDtoNoPass(any(Users.class));
    }

    @Test
    public void testUsersByRole() throws NotFoundException {
        List<Users> mockUsers = List.of(
                new Users(1, "john_doe", UserRole.ADMIN.toString(), "password123"));

        when(userRepository.findByRole("ADMIN")).thenReturn(Optional.of(mockUsers));
        when(userMapper.toUserDtoNoPass(any(Users.class))).thenAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            return new UserDtoNoPass(user.getUsername(), UserRole.valueOf(user.getRole()));
        });

        List<UserDtoNoPass> users = userService.getByUserRole("ADMIN");

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("john_doe", users.get(0).getUsername());
        assertEquals(UserRole.ADMIN, users.get(0).getRole());

        verify(userRepository, times(1)).findByRole("ADMIN");
        verify(userMapper, times(1)).toUserDtoNoPass(any(Users.class)); // Verify the correct mapper method
    }


    @Test
    void testInsertUser() {
        try (MockedStatic<UserMapper> mockedMapper = mockStatic(UserMapper.class)) {
            mockedMapper.when(() -> userMapper.toUsers(inputUserDto)).thenReturn(savedUser);
            mockedMapper.when(() -> userMapper.toUserDto(savedUser)).thenReturn(returnedUserDto);

            UserDto result = userService.insertUser(inputUserDto);

            assertNotNull(result);
            assertEquals("john_doe", result.getUsername());
            assertEquals(UserRole.USER, result.getRole());
            assertEquals("encodedPassword", result.getPassword());
        }
    }

    @Test
    public void testVerify_SuccessfulAuthentication() {
        String token = "generated-jwt-token";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(jwtUtil.generateToken(authRequest.getUsername())).thenReturn(token);

        AuthResponse response = userService.verify(authRequest);

        assertNotNull(response);
        assertEquals(token, response.getToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(authRequest.getUsername());
    }

    @Test
    public void testVerify_InvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        AuthResponse response = userService.verify(authRequest);

        assertNotNull(response);
        assertEquals("Authentication failed: Invalid username or password", response.getToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    public void testVerify_UnexpectedError() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        AuthResponse response = userService.verify(authRequest);

        assertNotNull(response);
        assertEquals("Authentication failed: An unexpected error occurred", response.getToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
