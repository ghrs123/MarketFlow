package com.gustavo.marketflow.order.infrastructure.jpa;

import com.gustavo.marketflow.order.domain.OrderHistory;
import com.gustavo.marketflow.order.domain.OrderHistoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA adapter for order history persistence port.
 */
@Repository
public class OrderHistoryJpaRepositoryAdapter implements OrderHistoryRepository {

    private final SpringDataOrderHistoryJpaRepository springDataOrderHistoryJpaRepository;

    public OrderHistoryJpaRepositoryAdapter(SpringDataOrderHistoryJpaRepository springDataOrderHistoryJpaRepository) {
        this.springDataOrderHistoryJpaRepository = springDataOrderHistoryJpaRepository;
    }

    @Override
    public OrderHistory save(OrderHistory orderHistory) {
        OrderHistoryEntity entity = OrderHistoryMapper.toEntity(orderHistory);
        OrderHistoryEntity saved = springDataOrderHistoryJpaRepository.save(entity);
        return OrderHistoryMapper.toDomain(saved);
    }

    @Override
    public List<OrderHistory> findByOrderId(UUID orderId) {
        return springDataOrderHistoryJpaRepository.findByOrderIdOrderByOccurredAtAsc(orderId).stream()
                .map(OrderHistoryMapper::toDomain)
                .toList();
    }
}
