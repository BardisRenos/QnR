package com.example.qnr.controllers;

import com.example.qnr.dto.UserDto;
import com.example.qnr.dto.UserDtoNoPass;
import com.example.qnr.exception.GlobalExceptionHandler;
import com.example.qnr.exception.NotFoundException;
import com.example.qnr.resources.enums.UserRole;
import com.example.qnr.security.SecurityProperties;
import com.example.qnr.security.auth.AuthRequest;
import com.example.qnr.security.auth.AuthResponse;
import com.example.qnr.services.CustomUserDetailsService;
import com.example.qnr.services.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {UserController.class, GlobalExceptionHandler.class})
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserController userController;

    @MockitoBean
    private UserServiceImpl userServiceImpl;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private SecurityProperties securityProperties;

    @BeforeEach
    void setUp() {
        when(securityProperties.getTokenPrefix()).thenReturn("Bearer ");
    }

    @Test
    void testAddUser_SentAnewUser_WithSuccess() throws Exception {
        when(userServiceImpl.insertUser(Mockito.any()))
                .thenReturn(new UserDto("jane_doe", UserRole.ADMIN, "pass_123"));

        UserDto userDto = new UserDto();
        userDto.setPassword("pass_123");
        userDto.setRole(UserRole.ADMIN);
        userDto.setUsername("jane_doe");
        String content = (new ObjectMapper()).writeValueAsString(userDto);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1.0/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);

        MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content()
                        .string("{\"username\":\"jane_doe\",\"role\":\"ADMIN\",\"password\":\"pass_123\"}"));
    }

    @Test
    void testGet_allUsers_WithSuccess() throws Exception {
        List<UserDtoNoPass> mockUserDtos = List.of(
                new UserDtoNoPass("john_doe", UserRole.ADMIN),
                new UserDtoNoPass("jane_smith", UserRole.USER),
                new UserDtoNoPass("guest_user", UserRole.MANAGER)
        );

        when(userServiceImpl.getAllUsers()).thenReturn(mockUserDtos);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/v1.0/user/all");

        MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value("john_doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].username").value("jane_smith"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].username").value("guest_user"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].role").value(UserRole.ADMIN.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].role").value(UserRole.USER.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].role").value(UserRole.MANAGER.toString()));
    }

    @Test
    void testUserLogin_WithValid_Credentials() throws Exception {
        when(userServiceImpl.verify(Mockito.any())).thenReturn(new AuthResponse("ABC123"));
        AuthRequest authRequest = new AuthRequest("pass_123", "janedoe");
        String content = (new ObjectMapper()).writeValueAsString(authRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1.0/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);

        MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().string("{\"token\":\"ABC123\"}"));
    }

    @Test
    void testGetUsersByRole_thenStatusIsNotFound_NotFound() throws Exception {
        when(userServiceImpl.getByUserRole(Mockito.any())).thenThrow(new NotFoundException("An error occurred"));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/v1.0/user/{user_role}",
                "User role");

        MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentType("text/plain;charset=ISO-8859-1"))
                .andExpect(MockMvcResultMatchers.content().string("An error occurred"));
    }

    @Test
    void testGetUsersByRole_thenStatusIsOk_WithSuccess() throws Exception {
        List<UserDtoNoPass> mockUserDtos = List.of(
                new UserDtoNoPass("john_doe", UserRole.ADMIN),
                new UserDtoNoPass("jane_smith", UserRole.USER)
        );

        when(userServiceImpl.getByUserRole("User role")).thenReturn(mockUserDtos);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/v1.0/user/{user_role}", "User role");

        MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value("john_doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].username").value("jane_smith"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].role").value(UserRole.ADMIN.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].role").value(UserRole.USER.toString()));
    }

    @Test
    void testLogout_ValidToken_ShouldReturnOk() throws Exception {
        String token = "valid_token";
        String authHeader = "Bearer " + token;

        Mockito.doNothing().when(userDetailsService).blacklistToken(token);
        when(securityProperties.getTokenPrefix()).thenReturn("Bearer ");
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1.0/user/logout")
                .header("Authorization", authHeader);

        MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Logged out successfully"));
    }

    @Test
    void testLogout_MissingAuthorizationHeader_ShouldReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1.0/user/logout")
                .header("Authorization", "");

        MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Invalid token"));
    }

}
