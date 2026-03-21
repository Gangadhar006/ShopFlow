package com.shopflow.order_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.constant.KafkaTopics;
import com.shopflow.event.OrderCancelledEvent;
import com.shopflow.event.OrderCreatedEvent;
import com.shopflow.order_service.dto.CreateOrderRequest;
import com.shopflow.order_service.dto.OrderDto;
import com.shopflow.order_service.entity.Order;
import com.shopflow.order_service.entity.OrderItem;
import com.shopflow.order_service.entity.OrderStatus;
import com.shopflow.order_service.entity.OutboxEvent;
import com.shopflow.order_service.exception.InsufficientStockException;
import com.shopflow.order_service.exception.OrderNotFoundException;
import com.shopflow.order_service.exception.ProductNotFoundException;
import com.shopflow.order_service.repository.OrderRepository;
import com.shopflow.order_service.repository.OutboxEventRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${services.product-service.url}")
    private String productServiceUrl;
    private final WebClient productServiceClient;

    // Place order — atomic: save order + outbox in one transaction
    public OrderDto placeOrder(CreateOrderRequest request, UUID userId) {

        // 1. Fetch product details + validate stock via Product Service
        List<OrderItem> items = request.items().stream()
            .map(itemReq -> {
                ProductDto product = fetchProduct(itemReq.productId());

                if (product.stock() < itemReq.quantity()) {
                    throw new InsufficientStockException(
                        itemReq.productId(),
                        itemReq.quantity(),
                        product.stock()
                    );
                }
                return OrderItem.builder()
                    .productId(itemReq.productId())
                    .productName(product.name())
                    .quantity(itemReq.quantity())
                    .unitPrice(product.price())
                    .build();
            })
            .toList();

        // 2. Calculate total
        BigDecimal total = items.stream()
            .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Build order
        Order order = Order.builder()
            .userId(userId)
            .status(OrderStatus.PENDING)
            .totalAmount(total)
            .items(items)
            .build();

        items.forEach(i -> i.setOrder(order));

        //4.save order
        Order saved = orderRepository.save(order);

        // 5. Save outbox event — SAME TRANSACTION
        // If this fails, order also rolls back. Atomicity guaranteed.
        saveOutboxEvent(saved);

        log.info("Order placed: orderId={}, userId={}, total={}",
            saved.getId(), userId, total);

        return OrderDto.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderDto findById(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException(orderId);
        }
        return OrderDto.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> findMyOrders(UUID userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
            .map(OrderDto::from);
    }

    public OrderDto cancelOrder(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException(orderId);
        }

        order.transitionTo(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        saveCancelledOutboxEvent(saved);

        log.info("Order cancelled: orderId={}", orderId);
        return OrderDto.from(saved);
    }

    public void confirmOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.transitionTo(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        log.info("Order confirmed: orderId={}", orderId);
    }

    public void markPaymentFailed(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.transitionTo(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);
        log.info("Order payment failed: orderId={}", orderId);
    }

    private void saveOutboxEvent(Order order) {
        try {
            OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(order.getId().toString())
                .userId(order.getUserId().toString())
                .items(order.getItems().stream()
                    .map(i -> OrderCreatedEvent.OrderItemDto.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                    .toList())
                .totalAmount(order.getTotalAmount())
                .occuredAt(LocalDateTime.now())
                .build();

            outboxEventRepository.save(OutboxEvent.builder()
                .aggregateType("ORDER")
                .aggregateId(order.getId().toString())
                .eventType(KafkaTopics.ORDER_CREATED)
                .payload(objectMapper.writeValueAsString(event))
                .status("PENDING")
                .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                "Failed to serialize order event", e);
        }
    }

    private void saveCancelledOutboxEvent(Order order) {
        try {
            OrderCancelledEvent event = OrderCancelledEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(order.getId().toString())
                .userId(order.getUserId().toString())
                .reason("CUSTOMER_CANCELLED")
                .occuredAt(LocalDateTime.now())
                .build();

            outboxEventRepository.save(OutboxEvent.builder()
                .aggregateType("ORDER")
                .aggregateId(order.getId().toString())
                .eventType(KafkaTopics.ORDER_CANCELLED)
                .payload(objectMapper.writeValueAsString(event))
                .status("PENDING")
                .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                "Failed to serialize cancel event", e);
        }
    }

    private ProductDto fetchProduct(String productId) {
        return productServiceClient.get()
            .uri("/api/products/{id}", productId)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, response ->
                Mono.error(new ProductNotFoundException(productId)))
            .bodyToMono(ProductDto.class)
            .block(Duration.ofSeconds(3));
    }

    private record ProductDto(
        String id, String name, BigDecimal price, Integer stock) {
    }
}
