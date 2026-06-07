package com.gustavo.marketflow.order.infrastructure;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.order.domain.OrderStatus;
import com.gustavo.marketflow.order.infrastructure.jpa.OrderEntity;
import com.gustavo.marketflow.order.infrastructure.jpa.SpringDataOrderHistoryJpaRepository;
import com.gustavo.marketflow.order.infrastructure.jpa.SpringDataOrderJpaRepository;
import com.gustavo.marketflow.support.PostgreSqlContainerBaseTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OptimisticLockingTest extends PostgreSqlContainerBaseTest {

    @Autowired
    private SpringDataOrderJpaRepository springDataOrderJpaRepository;

    @Autowired
    private SpringDataOrderHistoryJpaRepository springDataOrderHistoryJpaRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setUp() {
        springDataOrderHistoryJpaRepository.deleteAll();
        springDataOrderJpaRepository.deleteAll();
    }

    @Test
    void concurrentUpdates_conflictOnVersion() {
        OrderEntity entity = new OrderEntity();
        entity.setId(UUID.randomUUID());
        entity.setClientId("C001");
        entity.setSymbol("AAPL");
        entity.setSide(OrderSide.BUY);
        entity.setQuantity(new BigDecimal("10"));
        entity.setPrice(new BigDecimal("120"));
        entity.setStatus(OrderStatus.NEW);
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        springDataOrderJpaRepository.save(entity);

        EntityManager em1 = entityManagerFactory.createEntityManager();
        EntityManager em2 = entityManagerFactory.createEntityManager();

        try {
            OrderEntity tx1 = em1.find(OrderEntity.class, entity.getId());
            OrderEntity tx2 = em2.find(OrderEntity.class, entity.getId());

            em1.getTransaction().begin();
            tx1.setStatus(OrderStatus.ACCEPTED);
            em1.getTransaction().commit();

            em2.getTransaction().begin();
            tx2.setStatus(OrderStatus.REJECTED);

            assertThatThrownBy(() -> {
                em2.merge(tx2);
                em2.getTransaction().commit();
            }).isInstanceOfAny(
                    jakarta.persistence.OptimisticLockException.class,
                    org.springframework.orm.ObjectOptimisticLockingFailureException.class
            );
        } finally {
            if (em1.getTransaction().isActive()) {
                em1.getTransaction().rollback();
            }
            if (em2.getTransaction().isActive()) {
                em2.getTransaction().rollback();
            }
            em1.close();
            em2.close();
        }
    }
}
