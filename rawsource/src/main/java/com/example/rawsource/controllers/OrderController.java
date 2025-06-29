package com.example.rawsource.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.rawsource.entities.dto.order.AddOrderDto;
import com.example.rawsource.entities.dto.order.OrderDto;
import com.example.rawsource.entities.dto.order.UpdateOrderStatusDto;
import com.example.rawsource.services.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(@Autowired OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('BUYER')")
    public ResponseEntity<OrderDto> createOrder(@RequestBody AddOrderDto orderInfo) {
        OrderDto createdOrder = orderService.createOrder(orderInfo);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUYER', 'PROVIDER')")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('BUYER', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable UUID id) {
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('PROVIDER', 'BUYER')")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable UUID id,
            @RequestBody UpdateOrderStatusDto updateOrderDto) {
        OrderDto updatedOrder = orderService.updateOrderStatus(id, updateOrderDto);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/myorders")
    @PreAuthorize("hasAuthority('BUYER')")
    public ResponseEntity<List<OrderDto>> getBuyerOrders() {
        List<OrderDto> orders = orderService.getOrdersByBuyer();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/provider/myorders")
    @PreAuthorize("hasAuthority('PROVIDER')")
    public ResponseEntity<List<OrderDto>> getProviderOrders() {
        List<OrderDto> orders = orderService.getOrdersByProvider();
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAuthority('BUYER')")
    public ResponseEntity<?> deleteOrder(@PathVariable UUID orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok("Order deleted successfully");
    }
}
