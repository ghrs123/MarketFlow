package com.gustavo.marketflow.order.infrastructure.jpa;

import com.gustavo.marketflow.order.domain.OrderHistory;

final class OrderHistoryMapper {

    private OrderHistoryMapper() {
    }

    static OrderHistoryEntity toEntity(OrderHistory history) {
        OrderHistoryEntity entity = new OrderHistoryEntity();
        entity.setId(history.id());
        entity.setOrderId(history.orderId());
        entity.setEventType(history.eventType());
        entity.setPreviousStatus(history.previousStatus());
        entity.setNewStatus(history.newStatus());
        entity.setOccurredAt(history.occurredAt());
        entity.setPayloadJson(history.payloadJson());
        entity.setCreatedAt(history.createdAt());
        return entity;
    }

    static OrderHistory toDomain(OrderHistoryEntity entity) {
        return new OrderHistory(
                entity.getId(),
                entity.getOrderId(),
                entity.getEventType(),
                entity.getPreviousStatus(),
                entity.getNewStatus(),
                entity.getOccurredAt(),
                entity.getPayloadJson(),
                entity.getCreatedAt()
        );
    }
}
