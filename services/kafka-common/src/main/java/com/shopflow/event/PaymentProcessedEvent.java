package com.shopflow.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private String eventId;
    private String orderId;
    private String paymentId;
    private BigDecimal amount;
    private String status;
    private String failureReason;
    private LocalDateTime occuredAt;
}
