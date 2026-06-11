package com.gustavo.marketflow.order.application;

import com.gustavo.marketflow.order.OrderTestData;
import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IdempotencyRegistryTest {

    @Test
    void findExisting_knownKey_returnsDurableOrder() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        IdempotencyRegistry registry = new IdempotencyRegistry(orderRepository);
        Order existingOrder = OrderTestData.valid();
        when(orderRepository.findByIdempotencyKey("REQ-123")).thenReturn(Optional.of(existingOrder));

        assertThat(registry.findExisting("REQ-123")).contains(existingOrder);
    }

    @Test
    void findExisting_unknownKey_returnsEmpty() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        IdempotencyRegistry registry = new IdempotencyRegistry(orderRepository);
        when(orderRepository.findByIdempotencyKey("REQ-UNKNOWN")).thenReturn(Optional.empty());

        assertThat(registry.findExisting("REQ-UNKNOWN")).isEmpty();
    }
}
