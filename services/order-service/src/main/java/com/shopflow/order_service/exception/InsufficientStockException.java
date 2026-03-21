package com.shopflow.order_service.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productId,
                                      int requested,
                                      int available) {
        super(String.format(
            "Insufficient stock for %s. Requested: %d, Available: %d",
            productId, requested, available));
    }
}
