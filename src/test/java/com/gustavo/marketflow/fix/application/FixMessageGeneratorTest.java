package com.gustavo.marketflow.fix.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.order.domain.OrderStatus;

import static org.assertj.core.api.Assertions.assertThat;

class FixMessageGeneratorTest {

    private static final Instant FIXED_TIME = Instant.parse("2026-01-15T10:30:00Z");

    private final FixMessageGenerator generator =
            new FixMessageGenerator(Clock.fixed(FIXED_TIME, ZoneOffset.UTC));

    @Test
    void generate_buyOrder_returnsExpectedSimulatedFixString() {
        UUID orderId = UUID.fromString("5e297495-95fa-4b0d-a33a-42a2f50e59e9");
        Order order = order(orderId, OrderSide.BUY);

        String result = generator.generate(order);

        assertThat(result).isEqualTo(
                "8=FIX.4.4|35=D|49=MARKETFLOW|56=SIMULATED_BROKER"
                        + "|11=5e297495-95fa-4b0d-a33a-42a2f50e59e9"
                        + "|55=AAPL|54=1|38=100|40=2|44=150.25|52=2026-01-15T10:30:00Z"
        );
    }

    @Test
    void generate_sellOrder_returnsSideCodeTwo() {
        Order order = order(UUID.randomUUID(), OrderSide.SELL);

        String result = generator.generate(order);

        assertThat(result).contains("|54=2|");
    }

    private Order order(UUID orderId, OrderSide side) {
        return new Order(
                orderId,
                "C001",
                "AAPL",
                side,
                new BigDecimal("100.00000000"),
                new BigDecimal("150.25000000"),
                OrderStatus.NEW,
                FIXED_TIME,
                FIXED_TIME
        );
    }
}
