package com.shopflow.order_service.exception;

import com.shopflow.order_service.entity.OrderStatus;

public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(OrderStatus current, OrderStatus next) {
        super(String.format("Invalid order state transfer from %s to %s", current.name(), next.name()));
    }
}
