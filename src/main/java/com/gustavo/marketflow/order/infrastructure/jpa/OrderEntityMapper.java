package com.gustavo.marketflow.order.infrastructure.jpa;

import com.gustavo.marketflow.order.domain.Order;

final class OrderEntityMapper {

    private OrderEntityMapper() {
    }

    static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.setClientId(order.getClientId());
        entity.setSymbol(order.getSymbol());
        entity.setSide(order.getSide());
        entity.setQuantity(order.getQuantity());
        entity.setPrice(order.getPrice());
        entity.setStatus(order.getStatus());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());
        return entity;
    }

    static Order toDomain(OrderEntity entity) {
        return new Order(
                entity.getId(),
                entity.getClientId(),
                entity.getSymbol(),
                entity.getSide(),
                entity.getQuantity(),
                entity.getPrice(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
