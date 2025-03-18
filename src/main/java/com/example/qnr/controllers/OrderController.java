package com.example.qnr.controllers;

import com.example.qnr.dto.OrderDto;
import com.example.qnr.exception.NotFoundException;
import com.example.qnr.services.OrderServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for managing orders.
 * This class exposes various endpoints to handle order-related operations.
 */
@RestController
@RequestMapping("/api/v1.0/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderServiceImpl orderService;

    /**
     * Retrieves all orders.
     *
     * @return a ResponseEntity containing a list of all orders.
     */
    @GetMapping("/all")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return new ResponseEntity<>(orderService.getAllOrders(), HttpStatus.OK);
    }

    /**
     * Retrieves orders by their status.
     *
     * @param status the status of the orders to retrieve (e.g., "pending", "completed").
     * @return a ResponseEntity containing a list of orders that match the provided status.
     * @throws NotFoundException if no orders are found with the given status.
     */
    @GetMapping("/{status}")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable String status) throws NotFoundException {
        return new ResponseEntity<>(orderService.getOrdersByStatus(status), HttpStatus.OK);
    }

    /**
     * Adds a new order.
     *
     * @param order the OrderDto object containing the details of the new order.
     * @return a ResponseEntity containing the created OrderDto.
     */
    @PostMapping("/add")
    public ResponseEntity<OrderDto> addNewOrder(@Valid @RequestBody OrderDto order) {
        return new ResponseEntity<>(orderService.insertNewOrder(order), HttpStatus.CREATED);
    }

    /**
     * Updates an existing order.
     *
     * @param order the OrderDto object containing the updated order information.
     * @param orderId the ID of the order to be updated.
     * @return a ResponseEntity containing the updated OrderDto.
     */
    @PutMapping("/update/{orderId}")
    public ResponseEntity<OrderDto> updateOrder(@RequestBody OrderDto order, @PathVariable Integer orderId) {
        return new ResponseEntity<>(orderService.updateOrder(order, orderId), HttpStatus.OK);
    }

    /**
     * Deletes an order by its ID.
     *
     * @param id the ID of the order to be deleted.
     * @return a ResponseEntity with a success message if the order was deleted,
     *         or a 404 Not Found response if the order was not found.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Integer id) {
        if (orderService.deleteOrder(id)) {
            return ResponseEntity.ok("Order with ID " + id + " has been deleted.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes orders in bulk based on their status.
     *
     * @param status the status of the orders to be deleted (e.g., "pending", "completed").
     * @return a ResponseEntity with a message indicating how many orders were deleted.
     */
    @DeleteMapping("/bulk-delete/{status}")
    public ResponseEntity<String> bulkDeleteOrders(@PathVariable String status) {
        int deletedCount = orderService.bulkDeleteOrdersByStatus(status);
        return ResponseEntity.ok(deletedCount + " orders deleted successfully.");
    }


    /**
     * Endpoint to retrieve filtered and paginated orders.
     *
     * @param status    The status to filter orders by (can be null to ignore).
     * @param startDate The start date to filter orders (can be null to ignore).
     * @param endDate   The end date to filter orders (can be null to ignore).
     * @param page      The page number (starting from 0).
     * @param size      The page size.
     * @return A paginated list of orders matching the filters.
     */
    @GetMapping("/orders")
    public Page<OrderDto> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return orderService.getFilteredOrders(status, startDate, endDate, page, size);
    }
}
