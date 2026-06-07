package com.gustavo.marketflow.order.infrastructure.jpa;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderRepository;
import com.gustavo.marketflow.order.domain.OrderStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter for order persistence port.
 */
@Repository
public class OrderJpaRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderJpaRepository springDataOrderJpaRepository;

    public OrderJpaRepositoryAdapter(SpringDataOrderJpaRepository springDataOrderJpaRepository) {
        this.springDataOrderJpaRepository = springDataOrderJpaRepository;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = OrderEntityMapper.toEntity(order);
        OrderEntity saved = springDataOrderJpaRepository.save(entity);
        return OrderEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return springDataOrderJpaRepository.findById(id)
                .map(OrderEntityMapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return springDataOrderJpaRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(OrderEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByFilters(String clientId, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return springDataOrderJpaRepository.search(clientId, status, pageable).stream()
                .map(OrderEntityMapper::toDomain)
                .toList();
    }

    @Override
    public long countByFilters(String clientId, OrderStatus status) {
        return springDataOrderJpaRepository.countByFilters(clientId, status);
    }
}
