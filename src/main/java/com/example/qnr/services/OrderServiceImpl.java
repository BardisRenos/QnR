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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing orders.
 * Provides methods for CRUD operations on orders, including caching for optimization.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = {"orders"})
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    /**
     * Retrieves all orders.
     *
     * @return a list of OrderDto representing all orders in the system.
     */
    @Override
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toOrderDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves orders based on their status from the cache or database.
     * The result is cached to optimize subsequent requests for the same status.
     *
     * @param status the status of the orders to retrieve (e.g., "pending", "completed").
     * @return a list of OrderDto representing orders with the given status.
     * @throws NotFoundException if no orders are found with the given status.
     */
    @Override
    @Cacheable(value = "orders", key = "#status")
    public List<OrderDto> getOrdersByStatus(String status) throws NotFoundException {
        return orderRepository.findOrdersByStatusSorted(status)
                .orElseThrow(() -> new NotFoundException("No orders found with status: " + status))
                .stream()
                .map(orderMapper::toOrderDto)
                .collect(Collectors.toList());
    }

    /**
     * Adds a new order to the system.
     *
     * @param orderDto the OrderDto object containing the order details.
     * @return the created OrderDto.
     */
    @Override
    public OrderDto insertNewOrder(OrderDto orderDto) {
        Order order = orderMapper.toOrder(orderDto);
        return orderMapper.toOrderDto(orderRepository.save(order));
    }

    /**
     * Updates an existing order in the system.
     * The updated order is saved to the database and its cache entry is updated.
     *
     * @param orderDto the OrderDto object containing the updated order details.
     * @param id the ID of the order to update.
     * @return the updated OrderDto.
     * @throws EntityNotFoundException if the order with the given ID does not exist.
     */
    @Override
    @CachePut(value = "orders", key = "#id")
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

    /**
     * Deletes an order by its ID.
     * The cache entry for the deleted order is also evicted.
     *
     * @param id the ID of the order to delete.
     * @return true if the order was successfully deleted, false if the order was not found.
     */
    @Override
    @CacheEvict(value = "orders", key = "#id")
    public boolean deleteOrder(Integer id) {
        return orderRepository.findById(id).map(order -> {
            orderRepository.delete(order);
            return true;
        }).orElse(false);
    }

    /**
     * Deletes orders in bulk based on their status.
     *
     * @param status the status of the orders to delete (e.g., "pending", "completed").
     * @return the number of orders deleted.
     */
    @Override
    public int bulkDeleteOrdersByStatus(String status) {
        return orderRepository.deleteOrdersByStatus(status);
    }

    /**
     * Retrieves a paginated and filtered list of orders based on the given filters.
     * This method validates that the provided filter parameters are not null or empty.
     *
     * @param status     The status to filter orders by (must not be null or empty).
     * @param startDate  The start date to filter orders (must not be null).
     * @param endDate    The end date to filter orders (must not be null).
     * @param page       The page number (starting from 0).
     * @param size       The page size.
     * @return A paginated list of orders matching the filters.
     * @throws IllegalArgumentException if any of the parameters are null or invalid.
     */
    public Page<OrderDto> getFilteredOrders(String status, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException("Status must not be null or empty.");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date must not be null.");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date must not be null.");
        }

        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findOrdersWithFilters(status, startDate, endDate, pageable)
                .map(orderMapper::toOrderDto);
    }

}
