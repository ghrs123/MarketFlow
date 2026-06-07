package com.gustavo.marketflow.order.application;

import com.gustavo.marketflow.order.domain.OrderHistoryRepository;
import com.gustavo.marketflow.order.domain.OrderRepository;
import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.order.domain.OrderStatus;
import com.gustavo.marketflow.order.infrastructure.jpa.OrderEntity;
import com.gustavo.marketflow.order.infrastructure.jpa.SpringDataOrderHistoryJpaRepository;
import com.gustavo.marketflow.order.infrastructure.jpa.SpringDataOrderJpaRepository;
import com.gustavo.marketflow.support.PostgreSqlContainerBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class OrderApplicationServiceTest extends PostgreSqlContainerBaseTest {

    @Autowired
    private OrderApplicationService service;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SpringDataOrderJpaRepository springDataOrderJpaRepository;

    @Autowired
    private SpringDataOrderHistoryJpaRepository springDataOrderHistoryJpaRepository;

    @MockBean
    private OrderHistoryRepository orderHistoryRepository;

    @BeforeEach
    void setUp() {
        springDataOrderHistoryJpaRepository.deleteAll();
        springDataOrderJpaRepository.deleteAll();
    }

    @Test
    void createOrder_rollbackWhenHistoryPersistenceFails() {
        doThrow(new IllegalStateException("history persistence failed"))
                .when(orderHistoryRepository)
                .save(any());

        assertThatThrownBy(() -> service.createOrder(
                "C001",
                "AAPL",
                OrderSide.BUY,
                new BigDecimal("10"),
                new BigDecimal("150.25")
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("history persistence failed");

        assertThat(orderRepository.countByFilters(null, null)).isZero();
    }

    @Test
    void findByFilters_returnsPersistedRows() {
        springDataOrderJpaRepository.saveAll(List.of(
                buildOrderEntity("C001", "AAPL", OrderStatus.NEW),
                buildOrderEntity("C001", "MSFT", OrderStatus.NEW),
                buildOrderEntity("C002", "GOOG", OrderStatus.NEW)
        ));

        assertThat(service.findByFilters("C001", OrderStatus.NEW, 0, 10).content()).hasSize(2);
    }

    private OrderEntity buildOrderEntity(String clientId, String symbol, OrderStatus status) {
        OrderEntity entity = new OrderEntity();
        entity.setId(UUID.randomUUID());
        entity.setClientId(clientId);
        entity.setSymbol(symbol);
        entity.setSide(OrderSide.BUY);
        entity.setQuantity(new BigDecimal("1.0"));
        entity.setPrice(new BigDecimal("10.0"));
        entity.setStatus(status);
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }
}
