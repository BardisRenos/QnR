package com.example.qnr.services;

import com.example.qnr.dao.OrderRepository;
import com.example.qnr.dto.OrderDto;
import com.example.qnr.exception.NotFoundException;
import com.example.qnr.mappers.OrderMapper;
import com.example.qnr.resources.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderServiceImpl orderService;

    @Mock
    private OrderMapper orderMapper;

    private Order order;
    private OrderDto orderDto;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, orderMapper);

        order = new Order(1, 123, "Test Order", "PENDING", LocalDateTime.now());
        orderDto = new OrderDto(123, "Test Order", "PENDING", order.getCreateDate());
        startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        endDate = LocalDateTime.of(2024, 12, 31, 23, 59);
    }

    @Test
    void getAllOrders_ShouldReturnOrder() {
        Order order1 = new Order(1, 123 ,"Order 1", "PENDING", LocalDateTime.of(2024, 3, 17, 12, 0));
        Order order2 = new Order(2, 124,"Order 2", "COMPLETED", LocalDateTime.of(2024, 3, 16, 10, 30));
        OrderDto orderDto1 = new OrderDto(123,"Order 1", "PENDING", LocalDateTime.of(2024, 3, 17, 12, 0));
        OrderDto orderDto2 = new OrderDto(124,"Order 2", "COMPLETED", LocalDateTime.of(2024, 3, 16, 10, 30));

        when(orderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));
        when(orderMapper.toOrderDto(order1)).thenReturn(orderDto1);
        when(orderMapper.toOrderDto(order2)).thenReturn(orderDto2);

        List<OrderDto> result = orderService.getAllOrders();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDescription()).isEqualTo("Order 1");
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
        assertThat(result.get(1).getDescription()).isEqualTo("Order 2");
        assertThat(result.get(1).getStatus()).isEqualTo("COMPLETED");

        verify(orderRepository).findAll();
        verify(orderMapper).toOrderDto(order1);
        verify(orderMapper).toOrderDto(order2);
    }

    @Test
    void getOrdersByStatus_ShouldReturnListOfOrders_WhenOrdersExist() throws NotFoundException {
        String status = "PENDING";
        Order order1 = new Order(1, 123, "Order 1", "PENDING", LocalDateTime.of(2024, 3, 17, 12, 0));
        Order order2 = new Order(2, 124,"Order 2", "PENDING", LocalDateTime.of(2024, 3, 16, 10, 30));
        OrderDto orderDto1 = new OrderDto(123,"Order 1", "PENDING", LocalDateTime.of(2024, 3, 17, 12, 0));
        OrderDto orderDto2 = new OrderDto(124,"Order 2", "PENDING", LocalDateTime.of(2024, 3, 16, 10, 30));

        when(orderRepository.findOrdersByStatusSorted(status)).thenReturn(Optional.of(Arrays.asList(order1, order2)));
        when(orderMapper.toOrderDto(order1)).thenReturn(orderDto1);
        when(orderMapper.toOrderDto(order2)).thenReturn(orderDto2);

        List<OrderDto> result = orderService.getOrdersByStatus(status);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDescription()).isEqualTo("Order 1");
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
        assertThat(result.get(1).getDescription()).isEqualTo("Order 2");
        assertThat(result.get(1).getStatus()).isEqualTo("PENDING");

        verify(orderRepository).findOrdersByStatusSorted(status);
        verify(orderMapper).toOrderDto(order1);
        verify(orderMapper).toOrderDto(order2);
    }

    @Test
    void getOrdersByStatus_ShouldThrowNotFoundException_WhenNoOrdersExist() {
        String status = "COMPLETED";
        when(orderRepository.findOrdersByStatusSorted(status)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrdersByStatus(status))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No orders found with status: COMPLETED");

        verify(orderRepository).findOrdersByStatusSorted(status); // Ensure method was called
    }

    @Test
    void updateOrder_ShouldUpdateExistingOrder_WhenOrderExists() {
        Integer orderId = 1;
        OrderDto orderDto = new OrderDto(123,"Updated Order", "SHIPPED", LocalDateTime.of(2024, 3, 17, 14, 0));
        Order existingOrder = new Order(1, orderId, "Old Order", "PENDING", LocalDateTime.of(2024, 3, 16, 10, 30));
        Order updatedOrder = new Order(1, orderId, "Updated Order", "SHIPPED", LocalDateTime.of(2024, 3, 17, 14, 0));
        OrderDto updatedOrderDto = new OrderDto(123, "Updated Order", "SHIPPED", LocalDateTime.of(2024, 3, 17, 14, 0));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(existingOrder)).thenReturn(updatedOrder);
        when(orderMapper.toOrderDto(any(Order.class))).thenReturn(updatedOrderDto);

        OrderDto result = orderService.updateOrder(orderDto, orderId);

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Updated Order");
        assertThat(result.getStatus()).isEqualTo("SHIPPED");

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(existingOrder);
        verify(orderMapper).toOrderDto(updatedOrder);
    }


    @Test
    void insertNewOrder_ShouldInsertOrderAndReturnOrderDto() {
        OrderDto orderDto = new OrderDto(123,"New Order", "PENDING", LocalDateTime.of(2025, 3, 17, 10, 0));
        Order order = new Order(1,null, "New Order", "PENDING", LocalDateTime.of(2025, 3, 17, 10, 0));
        Order savedOrder = new Order(1,124, "New Order", "PENDING", LocalDateTime.of(2025, 3, 17, 10, 0));
        OrderDto savedOrderDto = new OrderDto(124, "New Order", "PENDING", LocalDateTime.of(2025, 3, 17, 10, 0));

        when(orderMapper.toOrder(orderDto)).thenReturn(order);
        when(orderMapper.toOrderDto(savedOrder)).thenReturn(savedOrderDto);
        when(orderRepository.save(order)).thenReturn(savedOrder);

        OrderDto result = orderService.insertNewOrder(orderDto);

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("New Order");
        assertThat(result.getStatus()).isEqualTo("PENDING");

        verify(orderRepository).save(order);
    }

    @Test
    void deleteOrder_ShouldReturnTrue_WhenOrderExists() {
        Integer orderId = 1;
        Order order = new Order(1, orderId, "Test Order", "PENDING", null);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        boolean result = orderService.deleteOrder(orderId);

        assertThat(result).isTrue();
        verify(orderRepository).findById(orderId);
        verify(orderRepository).delete(order);
    }

    @Test
    void deleteOrder_ShouldReturnFalse_WhenOrderDoesNotExist() {
        Integer orderId = 1;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        boolean result = orderService.deleteOrder(orderId);

        assertThat(result).isFalse();
        verify(orderRepository).findById(orderId);
    }

    @Test
    void bulkDeleteOrdersByStatus_ShouldReturnNumberOfDeletedOrders() {
        String status = "CANCELLED";
        int deletedCount = 5;
        when(orderRepository.deleteOrdersByStatus(status)).thenReturn(deletedCount);

        int result = orderService.bulkDeleteOrdersByStatus(status);

        assertThat(result).isEqualTo(deletedCount);
        verify(orderRepository).deleteOrdersByStatus(status);
    }

    @Test
    void testGetFilteredOrders_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order), pageable, 1);

        when(orderRepository.findOrdersWithFilters("PENDING", startDate, endDate, pageable)).thenReturn(orderPage);
        when(orderMapper.toOrderDto(order)).thenReturn(orderDto);

        Page<OrderDto> result = orderService.getFilteredOrders("PENDING", startDate, endDate, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Order", result.getContent().get(0).getDescription());

        verify(orderRepository, times(1)).findOrdersWithFilters("PENDING", startDate, endDate, pageable);
        verify(orderMapper, times(1)).toOrderDto(order);
    }

    @Test
    void testGetFilteredOrders_NullStatus_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.getFilteredOrders(null, startDate, endDate, 0, 10));

        assertEquals("Status must not be null or empty.", exception.getMessage());
    }

    @Test
    void testGetFilteredOrders_EmptyStatus_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.getFilteredOrders("", startDate, endDate, 0, 10));

        assertEquals("Status must not be null or empty.", exception.getMessage());
    }

    @Test
    void testGetFilteredOrders_NullStartDate_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.getFilteredOrders("ACTIVE", null, endDate, 0, 10));

        assertEquals("Start date must not be null.", exception.getMessage());
    }

    @Test
    void testGetFilteredOrders_NullEndDate_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.getFilteredOrders("ACTIVE", startDate, null, 0, 10));

        assertEquals("End date must not be null.", exception.getMessage());
    }
}
