package com.shopflow.order_service.config;

import com.shopflow.constant.KafkaTopics;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public KafkaAdmin kafkaAdmin(@Value("${spring.kafka.bootstrap-servers}") String servers) {
        return new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, servers));
    }

    private NewTopic topic(String nane) {
        return TopicBuilder.name(nane)
            .partitions(3)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(7 * 24 * 60 * 60 * 1000L))
            .build();
    }

    private NewTopic dlqTopic(String name) {
        return TopicBuilder.name(name)
            .partitions(1)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(7 * 24 * 60 * 60 * 1000L))
            .build();
    }

    @Bean
    public NewTopic orderCreated() {
        return topic(KafkaTopics.ORDER_CREATED);
    }

    @Bean
    public NewTopic orderCancelled() {
        return topic(KafkaTopics.ORDER_CANCELLED);
    }

    @Bean
    public NewTopic paymentInitiated() {
        return topic(KafkaTopics.PAYMENT_INITIATED);
    }

    @Bean
    public NewTopic paymentProcessed() {
        return topic(KafkaTopics.PAYMENT_PROCESSED);
    }

    @Bean
    public NewTopic paymentFailed() {
        return topic(KafkaTopics.PAYMENT_FAILED);
    }

    @Bean
    public NewTopic inventoryReserved() {
        return topic(KafkaTopics.INVENTORY_RESERVED);
    }

    @Bean
    public NewTopic inventoryReleased() {
        return topic(KafkaTopics.INVENTORY_RELEASED);
    }

    @Bean
    public NewTopic inventoryUpdated() {
        return topic(KafkaTopics.INVENTORY_UPDATED);
    }

    @Bean
    public NewTopic notificationSend() {
        return topic(KafkaTopics.NOTIFICATION_SEND);
    }

    @Bean
    public NewTopic orderDlq() {
        return dlqTopic(KafkaTopics.ORDER_DLQ);
    }

    @Bean
    public NewTopic inventoryDlq() {
        return dlqTopic(KafkaTopics.INVENTORY_DLQ);
    }

    @Bean
    public NewTopic paymentDlq() {
        return dlqTopic(KafkaTopics.PAYMENT_DLQ);
    }
}
