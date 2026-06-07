package com.gustavo.marketflow.order.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataOrderHistoryJpaRepository extends JpaRepository<OrderHistoryEntity, UUID> {

    List<OrderHistoryEntity> findByOrderIdOrderByOccurredAtAsc(UUID orderId);
}
