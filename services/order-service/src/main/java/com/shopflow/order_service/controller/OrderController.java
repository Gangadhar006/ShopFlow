package com.shopflow.order_service.controller;

import com.shopflow.order_service.dto.CreateOrderRequest;
import com.shopflow.order_service.dto.OrderDto;
import com.shopflow.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Place a new order")
    public OrderDto placeOrder(@Valid @RequestBody CreateOrderRequest request, Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return orderService.placeOrder(request, userId);
    }

    @GetMapping("{id}")
    @Operation(summary = "Get order by ID")
    public OrderDto getOrder(@PathVariable UUID id, Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return orderService.findById(id, userId);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my orders")
    public Page<OrderDto> getMyOrders(
        Authentication auth,
        @PageableDefault(size = 10, sort = "createdAt",
            direction = Sort.Direction.DESC) Pageable pageable) {
        UUID userId = UUID.fromString(auth.getName());
        return orderService.findMyOrders(userId, pageable);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public OrderDto cancelOrder(
        @PathVariable UUID id,
        Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return orderService.cancelOrder(id, userId);
    }
}
