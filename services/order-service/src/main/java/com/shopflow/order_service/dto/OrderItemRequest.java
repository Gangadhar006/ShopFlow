package com.shopflow.order_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
    @NotBlank String productId,
    @NotNull @Min(1) Integer quantity) {
}
