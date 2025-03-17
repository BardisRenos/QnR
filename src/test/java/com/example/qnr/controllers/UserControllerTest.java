package com.example.qnr.controllers;

import com.example.qnr.dto.UserDto;
import com.example.qnr.exception.GlobalExceptionHandler;
import com.example.qnr.exception.NotFoundException;
import com.example.qnr.resources.enums.UserRole;
import com.example.qnr.security.entities.AuthRequest;
import com.example.qnr.security.entities.AuthResponse;
import com.example.qnr.services.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.ArrayList;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {UserController.class, GlobalExceptionHandler.class})
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Autowired
    private UserController userController;

    @MockitoBean
    private UserServiceImpl userServiceImpl;

    @Test
    void testAddUser() throws Exception {
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
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content()
                        .string("{\"username\":\"jane_doe\",\"role\":\"ADMIN\",\"password\":\"pass_123\"}"));
    }

    @Test
    void testGetAllUsers() throws Exception {
        when(userServiceImpl.getAllUsers()).thenReturn(new ArrayList<>());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/v1.0/user/all");

        MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content().string("[]"));
    }

    @Test
    void testLogin() throws Exception {
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
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content().string("{\"token\":\"ABC123\"}"));
    }

    @Test
    void testGetUsersByRole_thenStatusIsNotFound() throws Exception {
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
    void testGetUsersByRole_thenStatusIsOk() throws Exception {
        when(userServiceImpl.getByUserRole(Mockito.any())).thenReturn(new ArrayList<>());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/v1.0/user/{user_role}",
                "User role");

        MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content().string("[]"));
    }
}
