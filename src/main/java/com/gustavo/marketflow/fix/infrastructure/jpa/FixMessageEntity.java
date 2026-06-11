package com.gustavo.marketflow.fix.infrastructure.jpa;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.gustavo.marketflow.fix.domain.FixMessage;

/**
 * JPA persistence model for simulated FIX messages.
 */
@Entity
@Table(name = "fix_messages")
public class FixMessageEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "raw_message", nullable = false, columnDefinition = "TEXT")
    private String rawMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public static FixMessageEntity from(FixMessage fixMessage) {
        FixMessageEntity entity = new FixMessageEntity();
        entity.id = fixMessage.id();
        entity.orderId = fixMessage.orderId();
        entity.rawMessage = fixMessage.rawMessage();
        entity.createdAt = fixMessage.createdAt();
        entity.updatedAt = fixMessage.updatedAt();
        return entity;
    }

    public FixMessage toDomain() {
        return new FixMessage(id, orderId, rawMessage, createdAt, updatedAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}
