package com.example.qnr.controllers;

import com.example.qnr.dto.OrderDto;
import com.example.qnr.exception.NotFoundException;
import com.example.qnr.services.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1.0/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderServiceImpl orderService;

    @GetMapping("/all")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return new ResponseEntity<>(orderService.getAllOrders(), HttpStatus.OK);
    }

    @GetMapping("/{status}")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable String status) throws NotFoundException {
        return new ResponseEntity<>(orderService.getOrdersByStatus(status), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<OrderDto> addNewOrder(@Valid @RequestBody OrderDto order) {
        return new ResponseEntity<>(orderService.insertNewOrder(order), HttpStatus.CREATED);
    }

    @PutMapping("/update/{orderId}")
    public ResponseEntity<OrderDto> updateOrder(@RequestBody OrderDto order, @PathVariable Integer orderId) {
        return new ResponseEntity<>(orderService.updateOrder(order, orderId), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Integer id) {
        if (orderService.deleteOrder(id)) {
            return ResponseEntity.ok("Order with ID " + id + " has been deleted.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/bulk-delete/{status}")
    public ResponseEntity<String> bulkDeleteOrders(@PathVariable String status) {
        int deletedCount = orderService.bulkDeleteOrdersByStatus(status);
        return ResponseEntity.ok(deletedCount + " orders deleted successfully.");
    }
}
