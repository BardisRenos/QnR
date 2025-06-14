package com.example.qnr.controllers;

import com.example.qnr.dto.OrderDto;
import com.example.qnr.exception.GlobalExceptionHandler;
import com.example.qnr.exception.NotFoundException;
import com.example.qnr.resources.enums.OrderStatus;
import com.example.qnr.services.OrderServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = {OrderController.class, GlobalExceptionHandler.class})
@WebMvcTest(controllers = OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private OrderController orderController;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private OrderServiceImpl orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderDto orderDto;
    private OrderDto updatedOrderDto;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
        orderDto = new OrderDto(1, "New Order", "SHIPPED", LocalDateTime.of(2024, 3, 17, 14, 0));
        updatedOrderDto = new OrderDto(2, "Updated Order", "SHIPPED", LocalDateTime.of(2024, 3, 17, 14, 0));
    }

    @Test
    void getAllOrders_ShouldReturnOrderList_WithSuccess() throws Exception {
        OrderDto order1 = new OrderDto(1,"Order 1", OrderStatus.PENDING.name(), LocalDateTime.now());
        OrderDto order2 = new OrderDto(2,"Order 2", OrderStatus.SHIPPED.name(), LocalDateTime.now());
        List<OrderDto> orders = Arrays.asList(order1, order2);

        when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/api/v1.0/order/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].description", is("Order 1")))
                .andExpect(jsonPath("$[1].status", is("SHIPPED")));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void getOrdersByStatus_ShouldReturnOrderList_WhenOrdersExist() throws Exception {
        String status = "Pending";
        OrderDto order = new OrderDto(1, "Order 1", OrderStatus.PENDING.name(), LocalDateTime.now());
        List<OrderDto> orders = List.of(order);

        when(orderService.getOrdersByStatus(status)).thenReturn(orders);

        mockMvc.perform(get("/api/v1.0/order/Pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].description", is("Order 1")));

        verify(orderService, times(1)).getOrdersByStatus(status);
    }

    @Test
    void getOrdersByStatus_ShouldReturnNotFound_WhenNoOrdersExist() throws Exception {
        String status = "Pending";

        when(orderService.getOrdersByStatus(status))
                .thenThrow(new NotFoundException("No orders found"));

        mockMvc.perform(get("/api/v1.0/order/Pending"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No orders found"));

        verify(orderService, times(1)).getOrdersByStatus(status);
    }

    @Test
    void addNewOrder_ShouldReturnCreatedOrder_WithSuccess() throws Exception {
        OrderDto order = new OrderDto(1, "New Order", OrderStatus.PENDING.name(), LocalDateTime.now());
        when(orderService.insertNewOrder(any(OrderDto.class))).thenReturn(order);

        mockMvc.perform(post("/api/v1.0/order/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", is("New Order")))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(orderService, times(1)).insertNewOrder(any(OrderDto.class));
    }

    @Test
    void updateOrder_ShouldReturnOk_WhenOrderIsUpdated() throws Exception {
        Integer orderId = 1;

        when(orderService.updateOrder(orderDto, orderId)).thenReturn(updatedOrderDto);

        mockMvc.perform(put("/api/v1.0/order/update/{orderId}", orderId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteOrder_ShouldReturnNotFound_WhenOrderDoesNotExist() throws Exception {
        Integer orderId = 1;
        when(orderService.deleteOrder(orderId)).thenReturn(false);

        mockMvc.perform(delete("/api/v1.0/order/delete/1"))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).deleteOrder(orderId);
    }

    @Test
    @Rollback(false)
    public void testDeleteOrder_whenOrderExists_WithSuccess() throws Exception {
        int orderId = 1;
        when(orderService.deleteOrder(orderId)).thenReturn(true);

        mockMvc.perform(delete("/api/v1.0/order/delete/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().string("Order with ID 1 has been deleted."));

        verify(orderService, times(1)).deleteOrder(orderId);
    }

    @Test
    void bulkDeleteOrders_ShouldReturnDeletedCount_WithSuccess() throws Exception {
        when(orderService.bulkDeleteOrdersByStatus("Pending")).thenReturn(5);

        mockMvc.perform(delete("/api/v1.0/order/bulk-delete/Pending"))
                .andExpect(status().isOk())
                .andExpect(content().string("5 orders deleted successfully."));
    }



    @Test
    public void testGetOrdersWithFilters_ShouldReturnOrderDto_WithSuccess() throws Exception {
        String status = "ACTIVE";
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 12, 31, 23, 59);
        int page = 0;
        int size = 10;

        String jwtToken = "your-valid-jwt-token";

        OrderDto orderDto = new OrderDto(1, "Test Order", "PENDING", startDate);
        Page<OrderDto> pageResult = new PageImpl<>(Collections.singletonList(orderDto), PageRequest.of(page, size), 1);

        when(orderService.getFilteredOrders(status, startDate, endDate, page, size)).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1.0/order/searchByFilter")
                .param("status", status)
                .param("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .param("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].description").value("Test Order"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }
}