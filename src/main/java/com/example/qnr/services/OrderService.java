package com.example.qnr.services;

import com.example.qnr.dto.OrderDto;
import com.example.qnr.exception.NotFoundException;

import java.util.List;

public interface OrderService {

    List<OrderDto> getAllOrders();
    List<OrderDto> getOrdersByStatus(String status) throws NotFoundException;
    OrderDto insertNewOrder(OrderDto orderDto);
    OrderDto updateOrder(OrderDto orderDto, Integer id);
    boolean deleteOrder(Integer id);
    int bulkDeleteOrdersByStatus(String status);
}
