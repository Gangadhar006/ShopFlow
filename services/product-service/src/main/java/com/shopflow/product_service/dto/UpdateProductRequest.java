package com.shopflow.product_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProductRequest(
    @DecimalMin("0.0") BigDecimal price,
    @Min(0) Integer stock,
    String description
) {
}
