package com.gustavo.marketflow.order.infrastructure.jpa;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gustavo.marketflow.order.domain.OrderStatus;

public interface SpringDataOrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    @Query("""
            select o
            from OrderEntity o
            where (:clientId is null or o.clientId = :clientId)
              and (:status is null or o.status = :status)
            """)
    Page<OrderEntity> search(
            @Param("clientId") String clientId,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    @Query("""
            select count(o)
            from OrderEntity o
            where (:clientId is null or o.clientId = :clientId)
              and (:status is null or o.status = :status)
            """)
    long countByFilters(
            @Param("clientId") String clientId,
            @Param("status") OrderStatus status
    );
}
