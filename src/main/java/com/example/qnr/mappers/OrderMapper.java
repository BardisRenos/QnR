package com.example.qnr.mappers;

import com.example.qnr.dto.OrderDto;
import com.example.qnr.resources.Order;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class OrderMapper {

    public OrderDto toOrderDto(Order order) {
        return new ModelMapper().map(order, OrderDto.class);
    }

    public Order toOrder(OrderDto orderDto) {
        return new ModelMapper().map(orderDto, Order.class);
    }
}
