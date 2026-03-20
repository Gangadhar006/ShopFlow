package com.shopflow.event;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservedEvent {
    private String eventId;
    private String orderId;
    private List<OrderCreatedEvent.OrderItemDto> items;
    private LocalDateTime occuredAt;
}
