package com.example.qnr.integrationTests;

import com.example.qnr.controllers.OrderController;
import com.example.qnr.dto.OrderDto;
import com.example.qnr.resources.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.redis.client-type=none",
        "spring.cache.type=none",
        "spring.session.store-type=none"
})
@Testcontainers
public class OrderControllerItTest {

    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.cache.type", () -> "none");
        registry.add("spring.session.store-type", () -> "none");
    }

    @Autowired
    private OrderController orderController;

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
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        insertTestData();
    }

    @AfterEach
    public void truncateOrdersTable() {
        String truncateSql = "TRUNCATE TABLE orders RESTART IDENTITY";
        jdbcTemplate.update(truncateSql);
    }

    private void insertTestData() {
        String insertSql = "INSERT INTO orders (description, status, create_date) VALUES (?, ?, ?)";

        List<Object[]> data = List.of(
                new Object[]{"Order 1 description", "Pending", Timestamp.valueOf(LocalDateTime.now())},
                new Object[]{"Order 2 description", "Completed", Timestamp.valueOf(LocalDateTime.now().plusHours(1))},
                new Object[]{"Order 3 description", "Pending", Timestamp.valueOf(LocalDateTime.now().plusHours(2))},
                new Object[]{"Order 4 description", "Completed", Timestamp.valueOf(LocalDateTime.now().plusHours(3))},
                new Object[]{"Order 5 description", "Pending", Timestamp.valueOf(LocalDateTime.now().plusHours(4))}
        );

        jdbcTemplate.batchUpdate(insertSql, data);
    }

    @Test
    void testGetAllOrders() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1.0/order/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(5)))
                .andExpect(jsonPath("$[0].description").value("Order 1 description"))
                .andExpect(jsonPath("$[0].status").value("Pending"))
                .andExpect(jsonPath("$[1].description").value("Order 2 description"))
                .andExpect(jsonPath("$[1].status").value("Completed"))
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<OrderDto> orders = objectMapper.readValue(jsonResponse, objectMapper.getTypeFactory().constructCollectionType(List.class, OrderDto.class));

        assertNotNull(orders);
        Assertions.assertEquals(5, orders.size());
        Assertions.assertEquals("Order 1 description", orders.get(0).getDescription());
        Assertions.assertEquals("Pending", orders.get(0).getStatus());
    }

    @Test
    void testGetOrdersByStatus() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1.0/order/Pending")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasSize(3)))
                .andExpect(jsonPath("$[0].description").value("Order 5 description"))
                .andExpect(jsonPath("$[0].status").value("Pending"))
                .andExpect(jsonPath("$[1].description").value("Order 3 description"))
                .andExpect(jsonPath("$[1].status").value("Pending"))
                .andExpect(jsonPath("$[2].description").value("Order 1 description"))
                .andExpect(jsonPath("$[2].status").value("Pending"))
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<OrderDto> orders = objectMapper.readValue(jsonResponse, objectMapper.getTypeFactory().constructCollectionType(List.class, OrderDto.class));

        assertNotNull(orders);
        Assertions.assertEquals(3, orders.size());
        Assertions.assertEquals("Order 5 description", orders.get(0).getDescription());
        Assertions.assertEquals("Pending", orders.get(0).getStatus());
    }

    @Test
    void testAddNewOrder() throws Exception {
        OrderDto newOrder = new OrderDto();
        newOrder.setDescription("New Order Description");
        newOrder.setStatus("Pending");

        String orderJson = objectMapper.writeValueAsString(newOrder);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1.0/order/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("New Order Description"))
                .andExpect(jsonPath("$.status").value("Pending"))
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        OrderDto createdOrder = objectMapper.readValue(jsonResponse, OrderDto.class);

        Assertions.assertEquals("New Order Description", createdOrder.getDescription());
        Assertions.assertEquals("Pending", createdOrder.getStatus());
    }

    @Test
    void testUpdateOrder() throws Exception {
        int orderId = 2;
        OrderDto updatedOrder = new OrderDto();
        updatedOrder.setDescription("Updated Description");
        updatedOrder.setStatus("Completed");
        updatedOrder.setCreateDate(LocalDateTime.now().plusDays(1));
        String orderJson = objectMapper.writeValueAsString(updatedOrder);

        mockMvc.perform(put("/api/v1.0/order/update/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.status").value("Completed"))
                .andReturn();

        String query = "SELECT order_id, description, status, create_date FROM orders WHERE order_id = ?";
        com.example.qnr.resources.Order order = jdbcTemplate.queryForObject(query, (rs, rowNum) -> {
            com.example.qnr.resources.Order o = new Order();
            o.setOrderId(rs.getInt("order_id"));
            o.setDescription(rs.getString("description"));
            o.setStatus(rs.getString("status"));
            o.setCreateDate(rs.getTimestamp("create_date").toLocalDateTime());
            return o;
        }, orderId);

        assertNotNull(order, "The order should exist after the update");
        assertEquals("Updated Description", order.getDescription());
        assertEquals("Completed", order.getStatus());
    }

    @Test
    void testDeleteOrder() throws Exception {
        String url = "/api/v1.0/order/delete/1";
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        assertTrue(response.contains("Order with ID 1 has been deleted"));

        String checkQuery = "SELECT COUNT(*) FROM orders WHERE order_id = 1";
        Integer count = jdbcTemplate.queryForObject(checkQuery, Integer.class);

        Assertions.assertEquals(0, count);
    }

    @Test
    void testDeleteNonExistentOrder() throws Exception {
        String url = "/api/v1.0/order/delete/999";

        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testBulkDeleteOrders() throws Exception {
        String countBeforeQuery = "SELECT COUNT(*) FROM orders WHERE status = 'Pending'";
        Integer countBefore = jdbcTemplate.queryForObject(countBeforeQuery, Integer.class);
        assertEquals(3, countBefore, "There should be 3 pending orders initially");

        String url = "/api/v1.0/order/bulk-delete/Pending";
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        assertTrue(response.contains("3 orders deleted successfully"));
        String checkQuery = "SELECT COUNT(*) FROM orders WHERE status = 'Pending'";

        Integer count = jdbcTemplate.queryForObject(checkQuery, Integer.class);
        Assertions.assertEquals(0, count);

        String checkCompletedQuery = "SELECT COUNT(*) FROM orders WHERE status = 'Pending'";
        Integer completedCount = jdbcTemplate.queryForObject(checkCompletedQuery, Integer.class);
        Assertions.assertEquals(0, completedCount);
    }

}
