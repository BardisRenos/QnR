package com.example.qnr.mappers;

import com.example.qnr.dto.OrderDto;
import com.example.qnr.resources.Order;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for mapping between Order entities and Order DTOs.
 * This class uses ModelMapper to convert between Order and OrderDto objects.
 */
@Service
public class OrderMapper {

    /**
     * Converts an Order entity to an OrderDto.
     *
     * @param order the Order entity to be converted.
     * @return an OrderDto that corresponds to the given Order entity.
     */
    public OrderDto toOrderDto(Order order) {
        return new ModelMapper().map(order, OrderDto.class);
    }

    /**
     * Converts an OrderDto to an Order entity.
     *
     * @param orderDto the OrderDto to be converted.
     * @return an Order entity that corresponds to the given OrderDto.
     */
    public Order toOrder(OrderDto orderDto) {
        return new ModelMapper().map(orderDto, Order.class);
    }
}
