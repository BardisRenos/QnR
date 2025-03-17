package com.example.qnr.services;

import com.example.qnr.dao.OrderRepository;
import com.example.qnr.dto.OrderDto;
import com.example.qnr.exception.NotFoundException;
import com.example.qnr.mappers.OrderMapper;
import com.example.qnr.resources.Order;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = {"orders"})
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toOrderDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "orders", key = "#status")
    public List<OrderDto> getOrdersByStatus(String status) throws NotFoundException {
        return orderRepository.findOrdersByStatusSorted(status)
                .orElseThrow(() -> new NotFoundException("No orders found with status: " + status))
                .stream()
                .map(orderMapper::toOrderDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto insertNewOrder(OrderDto orderDto) {
        Order order = orderMapper.toOrder(orderDto);
        return orderMapper.toOrderDto(orderRepository.save(order));
    }

    @Override
    public OrderDto updateOrder(OrderDto orderDto, Integer id) {
        log.info("Updating an Order entity with ID: {}", id);

        return orderRepository.findById(id).map(existingOrder -> {
            existingOrder.setDescription(orderDto.getDescription());
            existingOrder.setStatus(orderDto.getStatus());
            existingOrder.setCreateDate(orderDto.getCreateDate());

            Order updatedOrder = orderRepository.save(existingOrder);
            return orderMapper.toOrderDto(updatedOrder);
        }).orElseThrow(() -> new EntityNotFoundException("Order with ID " + id + " not found"));
    }

    @Override
    public boolean deleteOrder(Integer id) {
        return orderRepository.findById(id).map(order -> {
            orderRepository.delete(order);
            return true;
        }).orElse(false);
    }

    @Override
    public int bulkDeleteOrdersByStatus(String status) {
        return orderRepository.deleteOrdersByStatus(status);
    }
}
