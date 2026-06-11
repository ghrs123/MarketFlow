package com.gustavo.marketflow.fix.infrastructure.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository used by the FIX persistence adapter.
 */
public interface SpringDataFixMessageJpaRepository extends JpaRepository<FixMessageEntity, UUID> {

    Optional<FixMessageEntity> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);
}
