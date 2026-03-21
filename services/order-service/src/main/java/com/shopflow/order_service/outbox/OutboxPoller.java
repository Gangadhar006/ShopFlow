package com.shopflow.order_service.outbox;

import com.shopflow.order_service.entity.OutboxEvent;
import com.shopflow.order_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 1000)

    public void pollAndPublish() {
        List<OutboxEvent> pending = outboxEventRepository.findPendingEventsWithLock();

        if (pending.isEmpty()) return;

        log.debug("OutboxPoller: processing {} events", pending.size());

        for (OutboxEvent event : pending) {
            try {
                // Publish to Kafka — use aggregateId as partition key
                // Orders from same orderId always go to same partition

                kafkaTemplate.send(
                    event.getEventType(),
                    event.getAggregateId(),             // partition key
                    event.getPayload()
                ).get(5, TimeUnit.SECONDS);     // wait for ack

                // Mark as SENT only after Kafka confirms
                event.setStatus("SENT");
                event.setProcessedAt(LocalDateTime.now());
                outboxEventRepository.save(event);

                log.info("OutboxPoller: published eventType={} aggregateId={}", event.getEventType(), event.getAggregateId());

            } catch (Exception e) {
                log.error("OutboxPoller: failed to publish eventType={} aggregateId={} error={}",
                    event.getEventType(),
                    event.getAggregateId(),
                    e.getMessage());
            }
        }
    }
}
