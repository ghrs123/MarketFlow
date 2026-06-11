package com.gustavo.marketflow.order.application;

import com.gustavo.marketflow.event.infrastructure.InMemoryEventBus;
import com.gustavo.marketflow.monitoring.application.AuditLogService;
import com.gustavo.marketflow.monitoring.application.OrderMetricsService;
import com.gustavo.marketflow.order.OrderTestData;
import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderHistory;
import com.gustavo.marketflow.order.domain.OrderHistoryRepository;
import com.gustavo.marketflow.order.domain.OrderPage;
import com.gustavo.marketflow.order.domain.OrderRepository;
import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.order.domain.OrderStatus;
import com.gustavo.marketflow.shared.exception.OrderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderHistoryRepository orderHistoryRepository;

    private OrderApplicationService service;

    @BeforeEach
    void setUp() {
        service = new OrderApplicationService(
                orderRepository,
                orderHistoryRepository,
                new IdempotencyRegistry(orderRepository),
                new InMemoryEventBus(),
                new OrderMetricsService(new SimpleMeterRegistry()),
                mock(AuditLogService.class)
        );
    }

    @Test
    void createOrder_happyPath_persistsOrderAndHistory() {
        Order persistedOrder = OrderTestData.valid();
        when(orderRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(persistedOrder);
        when(orderHistoryRepository.save(any(OrderHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, OrderHistory.class));

        Order result = service.createOrder(
                "C001",
                "AAPL",
                OrderSide.BUY,
                new BigDecimal("10.00000000"),
                new BigDecimal("150.25000000")
        );

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        ArgumentCaptor<OrderHistory> historyCaptor = ArgumentCaptor.forClass(OrderHistory.class);

        verify(orderRepository).save(orderCaptor.capture());
        verify(orderHistoryRepository).save(historyCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        OrderHistory history = historyCaptor.getValue();

        assertThat(result).isSameAs(persistedOrder);
        assertThat(savedOrder.getClientId()).isEqualTo("C001");
        assertThat(savedOrder.getSymbol()).isEqualTo("AAPL");
        assertThat(savedOrder.getSide()).isEqualTo(OrderSide.BUY);
        assertThat(savedOrder.getQuantity()).isEqualByComparingTo("10.00000000");
        assertThat(savedOrder.getPrice()).isEqualByComparingTo("150.25000000");
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(history.orderId()).isEqualTo(persistedOrder.getId());
        assertThat(history.eventType()).isEqualTo("ORDER_CREATED");
        assertThat(history.previousStatus()).isNull();
        assertThat(history.newStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(history.occurredAt()).isNotNull();
        assertThat(history.createdAt()).isNotNull();
    }

    @Test
    void createOrder_rollbackWhenHistoryFails_orderDoesNotCompletePersistence() {
        Order persistedOrder = OrderTestData.valid();
        when(orderRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(persistedOrder);
        doThrow(new IllegalStateException("history persistence failed"))
                .when(orderHistoryRepository)
                .save(any(OrderHistory.class));

        assertThatThrownBy(() -> service.createOrder(
                "C001",
                "AAPL",
                OrderSide.BUY,
                new BigDecimal("10.00000000"),
                new BigDecimal("150.25000000")
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("history persistence failed");

        verify(orderRepository).save(any(Order.class));
        verify(orderHistoryRepository).save(any(OrderHistory.class));
    }

    @Test
    void createOrder_duplicateIdempotencyKey_returnsExistingOrderWithoutSaving() {
        Order existingOrder = OrderTestData.valid();
        when(orderRepository.findByIdempotencyKey("REQ-123")).thenReturn(Optional.of(existingOrder));

        Order result = service.createOrder(
                "C001",
                "AAPL",
                OrderSide.BUY,
                new BigDecimal("10.00000000"),
                new BigDecimal("150.25000000"),
                "REQ-123"
        );

        assertThat(result).isSameAs(existingOrder);
        org.mockito.Mockito.verify(orderRepository, org.mockito.Mockito.never()).save(any(Order.class));
        org.mockito.Mockito.verify(orderHistoryRepository, org.mockito.Mockito.never()).save(any(OrderHistory.class));
    }

    @Test
    void findById_existingOrder_returnsOrder() {
        Order existingOrder = OrderTestData.valid();
        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));

        Order result = service.findById(existingOrder.getId());

        assertThat(result).isSameAs(existingOrder);
    }

    @Test
    void findById_missingOrder_throwsOrderNotFoundException() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining(orderId.toString());
    }

    @Test
    void findByFilters_withClientIdAndStatus_returnsFilteredOrders() {
        List<Order> filteredOrders = List.of(
                OrderTestData.withClientId("C001"),
                OrderTestData.withStatus(OrderStatus.NEW)
        );
        when(orderRepository.findByFilters("C001", OrderStatus.NEW, 0, 20)).thenReturn(filteredOrders);
        when(orderRepository.countByFilters("C001", OrderStatus.NEW)).thenReturn(2L);

        OrderPage result = service.findByFilters("C001", OrderStatus.NEW, 0, 20);

        assertThat(result.content()).containsExactlyElementsOf(filteredOrders);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void findByFilters_paginated_returnsExpectedPageMetadata() {
        List<Order> pageContent = List.of(
                OrderTestData.withSymbol("MSFT"),
                OrderTestData.withSymbol("GOOG")
        );
        when(orderRepository.findByFilters(eq(null), eq(OrderStatus.NEW), eq(1), eq(2))).thenReturn(pageContent);
        when(orderRepository.countByFilters(null, OrderStatus.NEW)).thenReturn(5L);

        OrderPage result = service.findByFilters(null, OrderStatus.NEW, 1, 2);

        assertThat(result.content()).containsExactlyElementsOf(pageContent);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
    }
}
