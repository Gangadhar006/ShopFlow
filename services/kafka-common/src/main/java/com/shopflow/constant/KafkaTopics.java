package com.shopflow.constant;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_CANCELLED = "order.cancelled";
    public static final String PAYMENT_INITIATED = "payment.initiated";
    public static final String PAYMENT_PROCESSED = "payment.processed";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String INVENTORY_RESERVED = "inventory.reserved";
    public static final String INVENTORY_RELEASED = "inventory.released";
    public static final String INVENTORY_UPDATED = "inventory.updated";
    public static final String NOTIFICATION_SEND = "notification.send";
    public static final String ORDER_DLQ = "order.dlq";
    public static final String INVENTORY_DLQ = "inventory.dlq";
    public static final String PAYMENT_DLQ = "payment.dlq";
}
