package com.example.qnr.mapper;

import com.example.qnr.dto.OrderDto;
import com.example.qnr.mappers.OrderMapper;
import com.example.qnr.resources.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrderMapperTest {

    @InjectMocks
    private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper();
    }

    @Test
    void toOrderDto_ShouldMapOrderToOrderDto_WithSuccess() {
        Order order = new Order(1, 123,"Test Order", "PENDING", LocalDateTime.of(2024, 3, 17, 10, 0));
        OrderDto orderDto = orderMapper.toOrderDto(order);

        assertThat(orderDto).isNotNull();
        assertThat(orderDto.getOrderId()).isEqualTo(123);
        assertThat(orderDto.getDescription()).isEqualTo("Test Order");
        assertThat(orderDto.getStatus()).isEqualTo("PENDING");
        assertThat(orderDto.getCreateDate()).isEqualTo(LocalDateTime.of(2024, 3, 17, 10, 0));
    }

    @Test
    void toOrder_ShouldMapOrderDtoToOrder_WithSuccess() {
        OrderDto orderDto = new OrderDto(1, "Test Order", "PENDING", LocalDateTime.of(2024, 3, 17, 10, 0));
        Order order = orderMapper.toOrder(orderDto);

        assertThat(order).isNotNull();
        assertThat(order.getDescription()).isEqualTo("Test Order");
        assertThat(order.getStatus()).isEqualTo("PENDING");
        assertThat(order.getCreateDate()).isEqualTo(LocalDateTime.of(2024, 3, 17, 10, 0));
    }
}
