package com.shopflow.order_service.repository;

import com.shopflow.order_service.entity.Order;
import com.shopflow.order_service.entity.OrderStatus;
import com.shopflow.order_service.entity.OutboxEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    @Query(value = """
        SELECT * FROM outbox_events
        WHERE status = 'PENDING'
        ORDER BY created_at ASC
        LIMIT 10
        FOR UPDATE SKIP LOCKED""",
        nativeQuery = true)
    List<OutboxEvent> findPendingEventsWithLock();
}
