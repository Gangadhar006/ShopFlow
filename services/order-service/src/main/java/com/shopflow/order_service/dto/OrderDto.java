package com.shopflow.order_service.dto;


import com.shopflow.order_service.entity.Order;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.shopflow.event.OrderCreatedEvent.OrderItemDto;

@Builder
public record OrderDto(
    String id,
    String userId,
    String status,
    BigDecimal totalAmount,
    List<OrderItemDto> items,
    LocalDateTime createdAt) {

    public record OrderItemDto(
        String productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice
    ) {
    }

    public static OrderDto from(Order order) {
        return OrderDto.builder()
            .id(order.getId().toString())
            .userId(order.getUserId().toString())
            .status(order.getStatus().name())
            .totalAmount(order.getTotalAmount())
            .items(order.getItems().stream()
                .map(i -> new OrderItemDto(
                    i.getProductId(),
                    i.getProductName(),
                    i.getQuantity(),
                    i.getUnitPrice()))
                .toList())
            .createdAt(order.getCreatedAt())
            .build();
    }
}
