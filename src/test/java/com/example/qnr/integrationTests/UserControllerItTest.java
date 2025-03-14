package com.example.qnr.integrationTests;

import com.example.qnr.controllers.UserController;
import com.example.qnr.dto.UserDto;
import com.example.qnr.resources.enums.UserRole;
import com.example.qnr.security.entities.AuthRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.redis.client-type=none",
        "spring.cache.type=none",
        "spring.session.store-type=none"
})
@Testcontainers
public class UserControllerItTest {

    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Autowired
    private UserController userController;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public static void setUpAll() {
        postgresContainer.start();
    }

    @AfterAll
    public static void tearDownAll() {
        postgresContainer.stop();
    }

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        insertTestData();
    }

    @AfterEach
    public void truncateUsersTable() {
        String truncateSql = "TRUNCATE TABLE users RESTART IDENTITY";
        jdbcTemplate.update(truncateSql);
    }

    private void insertTestData() {
        String insertSql = "INSERT INTO users (username, role, password) VALUES (?, ?, ?)";

        List<Object[]> data = List.of(
                new Object[]{"user1", "ADMIN", "password1"},
                new Object[]{"user2", "USER", "password2"},
                new Object[]{"user3", "USER", "password3"}
        );

        jdbcTemplate.batchUpdate(insertSql, data);
    }

    @Test
    void testGetAllUsers() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1.0/user/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(3)))  // Adjust based on inserted test data
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<UserDto> users = objectMapper.readValue(jsonResponse, objectMapper.getTypeFactory().constructCollectionType(List.class, UserDto.class));

        assertNotNull(users);
        Assertions.assertEquals(3, users.size());
    }

    @Test
    void testGetUsersByRole() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1.0/user/USER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))  // Adjust based on inserted test data
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<UserDto> users = objectMapper.readValue(jsonResponse, objectMapper.getTypeFactory().constructCollectionType(List.class, UserDto.class));

        assertNotNull(users);
        Assertions.assertEquals(2, users.size());
    }

    @Test
    void testAddUser() throws Exception {
        UserDto newUser = new UserDto();
        newUser.setUsername("newUser");
        newUser.setRole(UserRole.USER);
        newUser.setPassword("newPassword");

        String userJson = objectMapper.writeValueAsString(newUser);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1.0/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        UserDto createdUser = objectMapper.readValue(jsonResponse, UserDto.class);

        Assertions.assertEquals("newUser", createdUser.getUsername());
        Assertions.assertEquals("USER", createdUser.getRole().name());
    }

    @Test
    void testLogin() throws Exception {
        AuthRequest authRequest = new AuthRequest("user1", "password1");

        String authJson = objectMapper.writeValueAsString(authRequest);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1.0/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        Assertions.assertTrue(jsonResponse.contains("token"));
    }
}
