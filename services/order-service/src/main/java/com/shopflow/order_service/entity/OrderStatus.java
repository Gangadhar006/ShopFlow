package com.shopflow.order_service.entity;

import com.shopflow.order_service.exception.InvalidOrderStateException;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PAYMENT_FAILED,
    CANCELLED,
    SHIPPED,
    DELIVERED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
        PENDING, Set.of(CONFIRMED, PAYMENT_FAILED, CANCELLED),
        CONFIRMED, Set.of(SHIPPED, CANCELLED),
        PAYMENT_FAILED, Set.of(CANCELLED),
        SHIPPED, Set.of(DELIVERED),
        CANCELLED, Set.of(),
        DELIVERED, Set.of()
    );

    public void validateTransition(OrderStatus next) {
        if (!ALLOWED.get(this).contains(next)) {
            throw new InvalidOrderStateException(this, next);
        }
    }
}
