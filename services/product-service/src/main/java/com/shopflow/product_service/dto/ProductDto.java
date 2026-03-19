package com.shopflow.product_service.dto;

import com.shopflow.product_service.document.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ProductDto(
    String id,
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    String category,
    List<String> images,
    LocalDateTime createdAt
) {
    public static ProductDto from(Product product) {
        return ProductDto.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .stock(product.getStock())
            .category(product.getCategory())
            .images(product.getImages())
            .createdAt(product.getCreatedAt())
            .build();
    }
}
