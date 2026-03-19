package com.shopflow.product_service.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String id, int requested, int available) {
        super(String.format("Insufficient stock for product: %d, requested: %d, available: %d", id, requested, available));
    }
}
