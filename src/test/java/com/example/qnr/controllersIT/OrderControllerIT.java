package com.example.qnr.controllersIT;

import com.example.qnr.controllers.OrderController;
import com.example.qnr.dto.OrderDto;
import com.example.qnr.resources.Order;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.security.test.context.support.WithMockUser;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class OrderControllerIT {

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
        String insertSql = "INSERT INTO orders (order_id, description, status, create_date) VALUES (?, ?, ?, ?)";

        List<Object[]> data = List.of(
                new Object[]{1, "Order 1 description", "Pending", Timestamp.valueOf(LocalDateTime.now())},
                new Object[]{2, "Order 2 description", "Completed", Timestamp.valueOf(LocalDateTime.now().plusHours(1))},
                new Object[]{3, "Order 3 description", "Pending", Timestamp.valueOf(LocalDateTime.now().plusHours(2))},
                new Object[]{4, "Order 4 description", "Completed", Timestamp.valueOf(LocalDateTime.now().plusHours(3))},
                new Object[]{5, "Order 5 description", "Pending", Timestamp.valueOf(LocalDateTime.now().plusHours(4))}
        );

        jdbcTemplate.batchUpdate(insertSql, data);
    }

    @Test
    void getAllOrders_ShouldReturnFiveOrders_WhenCalled() throws Exception {
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
        assertEquals(5, orders.size());
        assertEquals("Order 1 description", orders.get(0).getDescription());
        assertEquals("Pending", orders.get(0).getStatus());
    }

    @Test
    void getOrdersByStatus_ShouldReturnThreePendingOrders_WhenStatusIsPending() throws Exception {
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
        assertEquals(3, orders.size());
        assertEquals("Order 5 description", orders.get(0).getDescription());
        assertEquals("Pending", orders.get(0).getStatus());
    }

    @Test
    void addNewOrder_ShouldCreateOrder_WhenOrderIsValid() throws Exception {
        OrderDto newOrder = new OrderDto();
        newOrder.setOrderId(1);
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

        assertEquals("New Order Description", createdOrder.getDescription());
        assertEquals("Pending", createdOrder.getStatus());
    }

    @Test
    void updateOrder_ShouldUpdateOrder_WhenOrderIsValid() throws Exception {
        int orderId = 2;
        OrderDto updatedOrder = new OrderDto();
        updatedOrder.setOrderId(2);
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

        String query = "SELECT id, order_id, description, status, create_date FROM orders WHERE order_id = ?";
        com.example.qnr.resources.Order order = jdbcTemplate.queryForObject(query, (rs, rowNum) -> {
            com.example.qnr.resources.Order o = new Order();
            o.setId(rs.getInt("id"));
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteOrder_ShouldDeleteOrder_WhenOrderExists() throws Exception {
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

        assertEquals(0, count);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteOrder_ShouldReturnNotFound_WhenOrderDoesNotExist() throws Exception {
        String url = "/api/v1.0/order/delete/999";

        mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void bulkDeleteOrders_ShouldDeleteAllPendingOrders_WhenStatusIsPending() throws Exception {
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
        assertEquals(0, count);
    }

    @Test
    void getOrdersByDateRange_ShouldReturnOrdersInRange_WhenDateRangeIsProvided() throws Exception {
        String status = "Pending";
        LocalDateTime startDate = LocalDateTime.now().minusMinutes(10);
        LocalDateTime endDate = LocalDateTime.now().plusHours(4).plusMinutes(10);
        int page = 0;
        int size = 10;
        String jwtToken = "your-valid-jwt-token";

        MvcResult mvcResult = mockMvc.perform(get("/api/v1.0/order/searchByFilter")
                        .param("status", status)
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].description").value("Order 1 description"))
                .andExpect(jsonPath("$.content[0].status").value("Pending"))
                .andExpect(jsonPath("$.content[1].description").value("Order 3 description"))
                .andExpect(jsonPath("$.content[1].status").value("Pending"))
                .andExpect(jsonPath("$.content[2].description").value("Order 5 description"))
                .andExpect(jsonPath("$.content[2].status").value("Pending"))
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        String contentJson = rootNode.get("content").toString();

        List<OrderDto> orders = objectMapper.readValue(contentJson, objectMapper.getTypeFactory().constructCollectionType(List.class, OrderDto.class));

        assertNotNull(orders);
        assertEquals(3, orders.size());
        assertEquals("Order 1 description", orders.get(0).getDescription());
        assertEquals("Pending", orders.get(0).getStatus());
    }
}
