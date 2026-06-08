package com.gustavo.marketflow.order.infrastructure;

import com.gustavo.marketflow.order.OrderTestData;
import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderHistory;
import com.gustavo.marketflow.order.domain.OrderHistoryRepository;
import com.gustavo.marketflow.order.domain.OrderRepository;
import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.order.domain.OrderStatus;
import com.gustavo.marketflow.order.infrastructure.jpa.OrderEntity;
import com.gustavo.marketflow.order.infrastructure.jpa.SpringDataOrderHistoryJpaRepository;
import com.gustavo.marketflow.order.infrastructure.jpa.SpringDataOrderJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class OrderRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("marketflow")
                    .withUsername("marketflow")
                    .withPassword("marketflow");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @Autowired
    private SpringDataOrderJpaRepository springDataOrderJpaRepository;

    @Autowired
    private SpringDataOrderHistoryJpaRepository springDataOrderHistoryJpaRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void setUp() {
        springDataOrderHistoryJpaRepository.deleteAll();
        springDataOrderJpaRepository.deleteAll();
    }

    @Test
    void saveAndFindById_persistsAndLoadsOrder() {
        Order order = persistOrder(OrderTestData.valid());

        Order found = orderRepository.findById(order.getId()).orElseThrow();

        assertThat(found.getId()).isEqualTo(order.getId());
        assertThat(found.getClientId()).isEqualTo(order.getClientId());
        assertThat(found.getSymbol()).isEqualTo(order.getSymbol());
        assertThat(found.getSide()).isEqualTo(order.getSide());
        assertThat(found.getQuantity()).isEqualByComparingTo(order.getQuantity());
        assertThat(found.getPrice()).isEqualByComparingTo(order.getPrice());
        assertThat(found.getStatus()).isEqualTo(order.getStatus());
    }

    @Test
    void findByFilters_withClientId_returnsOnlyClientOrders() {
        persistOrder(withClientStatusAndTimestamp("C001", OrderStatus.NEW, Instant.parse("2026-01-15T10:30:00Z")));
        persistOrder(withClientStatusAndTimestamp("C001", OrderStatus.ACCEPTED, Instant.parse("2026-01-15T10:31:00Z")));
        persistOrder(withClientStatusAndTimestamp("C002", OrderStatus.NEW, Instant.parse("2026-01-15T10:32:00Z")));

        List<Order> result = orderRepository.findByFilters("C001", null, 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Order::getClientId).containsOnly("C001");
    }

    @Test
    void findByFilters_withStatus_returnsOnlyOrdersWithThatStatus() {
        persistOrder(withClientStatusAndTimestamp("C001", OrderStatus.NEW, Instant.parse("2026-01-15T10:30:00Z")));
        persistOrder(withClientStatusAndTimestamp("C002", OrderStatus.ACCEPTED, Instant.parse("2026-01-15T10:31:00Z")));
        persistOrder(withClientStatusAndTimestamp("C003", OrderStatus.ACCEPTED, Instant.parse("2026-01-15T10:32:00Z")));

        List<Order> result = orderRepository.findByFilters(null, OrderStatus.ACCEPTED, 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Order::getStatus).containsOnly(OrderStatus.ACCEPTED);
    }

    @Test
    void findByFilters_withClientIdAndStatus_returnsCombinedMatches() {
        persistOrder(withClientStatusAndTimestamp("C001", OrderStatus.NEW, Instant.parse("2026-01-15T10:30:00Z")));
        persistOrder(withClientStatusAndTimestamp("C001", OrderStatus.ACCEPTED, Instant.parse("2026-01-15T10:31:00Z")));
        persistOrder(withClientStatusAndTimestamp("C002", OrderStatus.ACCEPTED, Instant.parse("2026-01-15T10:32:00Z")));

        List<Order> result = orderRepository.findByFilters("C001", OrderStatus.ACCEPTED, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getClientId()).isEqualTo("C001");
        assertThat(result.getFirst().getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    void findByFilters_paginated_returnsCorrectPages() {
        persistOrder(withClientStatusSymbolAndTimestamp("C001", OrderStatus.NEW, "AAPL-1", Instant.parse("2026-01-15T10:30:00Z")));
        persistOrder(withClientStatusSymbolAndTimestamp("C001", OrderStatus.NEW, "AAPL-2", Instant.parse("2026-01-15T10:31:00Z")));
        persistOrder(withClientStatusSymbolAndTimestamp("C001", OrderStatus.NEW, "AAPL-3", Instant.parse("2026-01-15T10:32:00Z")));

        List<Order> page0 = orderRepository.findByFilters("C001", OrderStatus.NEW, 0, 2);
        List<Order> page1 = orderRepository.findByFilters("C001", OrderStatus.NEW, 1, 2);

        assertThat(page0).hasSize(2);
        assertThat(page0).extracting(Order::getSymbol).containsExactly("AAPL-3", "AAPL-2");
        assertThat(page1).hasSize(1);
        assertThat(page1.getFirst().getSymbol()).isEqualTo("AAPL-1");
    }

    @Test
    void countByFilters_countsCorrectly() {
        persistOrder(withClientStatusAndTimestamp("C001", OrderStatus.NEW, Instant.parse("2026-01-15T10:30:00Z")));
        persistOrder(withClientStatusAndTimestamp("C001", OrderStatus.NEW, Instant.parse("2026-01-15T10:31:00Z")));
        persistOrder(withClientStatusAndTimestamp("C001", OrderStatus.ACCEPTED, Instant.parse("2026-01-15T10:32:00Z")));
        persistOrder(withClientStatusAndTimestamp("C002", OrderStatus.NEW, Instant.parse("2026-01-15T10:33:00Z")));

        long count = orderRepository.countByFilters("C001", OrderStatus.NEW);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void findByFilters_withoutFilters_returnsAllOrders() {
        persistOrder(withClientStatusAndTimestamp("C001", OrderStatus.NEW, Instant.parse("2026-01-15T10:30:00Z")));
        persistOrder(withClientStatusAndTimestamp("C002", OrderStatus.ACCEPTED, Instant.parse("2026-01-15T10:31:00Z")));
        persistOrder(withClientStatusAndTimestamp("C003", OrderStatus.REJECTED, Instant.parse("2026-01-15T10:32:00Z")));

        List<Order> result = orderRepository.findByFilters(null, null, 0, 10);

        assertThat(result).hasSize(3);
    }

    @Test
    void history_persistsAndLoadsByOrderIdOrderedByOccurredAt() {
        Order order = persistOrder(OrderTestData.valid());
        orderHistoryRepository.save(new OrderHistory(
                UUID.randomUUID(),
                order.getId(),
                "ORDER_ACCEPTED",
                OrderStatus.NEW,
                OrderStatus.ACCEPTED,
                Instant.parse("2026-01-15T10:35:00Z"),
                null,
                Instant.parse("2026-01-15T10:35:00Z")
        ));
        orderHistoryRepository.save(new OrderHistory(
                UUID.randomUUID(),
                order.getId(),
                "ORDER_CREATED",
                null,
                OrderStatus.NEW,
                Instant.parse("2026-01-15T10:30:00Z"),
                null,
                Instant.parse("2026-01-15T10:30:00Z")
        ));

        List<OrderHistory> history = orderHistoryRepository.findByOrderId(order.getId());

        assertThat(history).hasSize(2);
        assertThat(history).extracting(OrderHistory::eventType)
                .containsExactly("ORDER_CREATED", "ORDER_ACCEPTED");
        assertThat(history).extracting(OrderHistory::orderId).containsOnly(order.getId());
    }

    @Test
    void optimisticLocking_secondConcurrentUpdateFails() {
        Order savedOrder = persistOrder(OrderTestData.valid());

        EntityManager em1 = entityManagerFactory.createEntityManager();
        EntityManager em2 = entityManagerFactory.createEntityManager();

        try {
            OrderEntity tx1 = em1.find(OrderEntity.class, savedOrder.getId());
            OrderEntity tx2 = em2.find(OrderEntity.class, savedOrder.getId());

            em1.getTransaction().begin();
            tx1.setStatus(OrderStatus.ACCEPTED);
            em1.getTransaction().commit();

            em2.getTransaction().begin();
            tx2.setStatus(OrderStatus.REJECTED);

            assertThatThrownBy(() -> {
                em2.merge(tx2);
                em2.getTransaction().commit();
            }).isInstanceOf(RollbackException.class)
                    .hasCauseInstanceOf(OptimisticLockException.class);
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

    private Order persistOrder(Order order) {
        return orderRepository.save(order);
    }

    private Order withClientStatusAndTimestamp(String clientId, OrderStatus status, Instant timestamp) {
        return withClientStatusSymbolAndTimestamp(clientId, status, "AAPL-" + timestamp.getEpochSecond(), timestamp);
    }

    private Order withClientStatusSymbolAndTimestamp(String clientId,
                                                     OrderStatus status,
                                                     String symbol,
                                                     Instant timestamp) {
        Order base = OrderTestData.valid();
        return new Order(
                UUID.randomUUID(),
                clientId,
                symbol,
                OrderSide.BUY,
                base.getQuantity(),
                base.getPrice(),
                status,
                timestamp,
                timestamp
        );
    }
}
