package com.gustavo.marketflow.order.application;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.order.domain.OrderStatus;
import com.gustavo.marketflow.order.infrastructure.OrderInMemoryRepository;
import com.gustavo.marketflow.shared.exception.OrderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure unit tests for the application service.
 *
 * <p>The service is exercised against the real in-memory repository
 * (which is itself a deterministic, side-effect-free collaborator), so no
 * mocking framework is needed. This keeps the test fast and focused on
 * the behaviour of the service, not on framework wiring.</p>
 */
class OrderApplicationServiceTest {

    private OrderApplicationService service;

    @BeforeEach
    void setUp() {
        service = new OrderApplicationService(new OrderInMemoryRepository());
    }

    @Test
    void createOrder_persistsOrderWithNewStatusAndGeneratedId() {
        Order created = service.createOrder(
                "C001",
                "AAPL",
                OrderSide.BUY,
                new BigDecimal("100"),
                new BigDecimal("150.25")
        );

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(created.getClientId()).isEqualTo("C001");
        assertThat(created.getSymbol()).isEqualTo("AAPL");
        assertThat(created.getSide()).isEqualTo(OrderSide.BUY);
        assertThat(created.getQuantity()).isEqualByComparingTo("100");
        assertThat(created.getPrice()).isEqualByComparingTo("150.25");
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getUpdatedAt()).isEqualTo(created.getCreatedAt());
    }

    @Test
    void findById_returnsPreviouslyCreatedOrder() {
        Order created = service.createOrder("C001", "AAPL", OrderSide.BUY,
                new BigDecimal("10"), new BigDecimal("1.50"));

        Order found = service.findById(created.getId());

        assertThat(found).isEqualTo(created);
    }

    @Test
    void findById_throwsWhenOrderDoesNotExist() {
        UUID randomId = UUID.randomUUID();

        assertThatThrownBy(() -> service.findById(randomId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining(randomId.toString());
    }

    @Test
    void findAll_returnsAllCreatedOrders() {
        service.createOrder("C001", "AAPL", OrderSide.BUY, new BigDecimal("1"), new BigDecimal("1"));
        service.createOrder("C002", "MSFT", OrderSide.SELL, new BigDecimal("2"), new BigDecimal("2"));

        assertThat(service.findAll()).hasSize(2);
    }
}
