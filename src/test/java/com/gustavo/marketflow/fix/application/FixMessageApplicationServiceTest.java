package com.gustavo.marketflow.fix.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gustavo.marketflow.event.infrastructure.InMemoryEventBus;
import com.gustavo.marketflow.fix.domain.FixMessage;
import com.gustavo.marketflow.fix.domain.FixMessageRepository;
import com.gustavo.marketflow.fix.domain.FixTagExplanation;
import com.gustavo.marketflow.order.OrderTestData;
import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderRepository;
import com.gustavo.marketflow.shared.exception.FixMessageAlreadyExistsException;
import com.gustavo.marketflow.shared.exception.FixMessageNotFoundException;
import com.gustavo.marketflow.shared.exception.InvalidFixMessageException;
import com.gustavo.marketflow.shared.exception.OrderNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FixMessageApplicationServiceTest {

    private static final Instant FIXED_TIME = Instant.parse("2026-01-15T10:30:00Z");

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private FixMessageRepository fixMessageRepository;

    private FixMessageApplicationService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(FIXED_TIME, ZoneOffset.UTC);
        FixMessageParser parser = new FixMessageParser();
        service = new FixMessageApplicationService(
                orderRepository,
                fixMessageRepository,
                new FixMessageGenerator(clock),
                new FixMessageExplainer(parser),
                new InMemoryEventBus(),
                clock,
                new SimpleMeterRegistry()
        );
    }

    @Test
    void generateForOrder_existingOrder_persistsGeneratedMessage() {
        Order order = OrderTestData.valid();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(fixMessageRepository.existsByOrderId(order.getId())).thenReturn(false);
        when(fixMessageRepository.save(any(FixMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FixMessage result = service.generateForOrder(order.getId());

        assertThat(result.orderId()).isEqualTo(order.getId());
        assertThat(result.rawMessage()).contains("8=FIX.4.4", "|54=1|", "|55=AAPL|");
        assertThat(result.createdAt()).isEqualTo(FIXED_TIME);
        verify(fixMessageRepository).save(any(FixMessage.class));
    }

    @Test
    void generateForOrder_missingOrder_throwsOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateForOrder(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining(orderId.toString());
        verify(fixMessageRepository, never()).save(any());
    }

    @Test
    void generateForOrder_existingFixMessage_throwsConflict() {
        Order order = OrderTestData.valid();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(fixMessageRepository.existsByOrderId(order.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.generateForOrder(order.getId()))
                .isInstanceOf(FixMessageAlreadyExistsException.class)
                .hasMessageContaining(order.getId().toString());
    }

    @Test
    void findByOrderId_existingMessage_returnsMessage() {
        Order order = OrderTestData.valid();
        FixMessage fixMessage = fixMessage(order.getId());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(fixMessageRepository.findByOrderId(order.getId())).thenReturn(Optional.of(fixMessage));

        assertThat(service.findByOrderId(order.getId())).isEqualTo(fixMessage);
    }

    @Test
    void findByOrderId_withoutGeneratedMessage_throwsNotFound() {
        Order order = OrderTestData.valid();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(fixMessageRepository.findByOrderId(order.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByOrderId(order.getId()))
                .isInstanceOf(FixMessageNotFoundException.class)
                .hasMessageContaining(order.getId().toString());
    }

    @Test
    void explainByOrderId_existingMessage_returnsTagExplanation() {
        Order order = OrderTestData.valid();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(fixMessageRepository.findByOrderId(order.getId())).thenReturn(Optional.of(fixMessage(order.getId())));

        List<FixTagExplanation> result = service.explainByOrderId(order.getId());

        assertThat(result).extracting(FixTagExplanation::name)
                .containsExactly("BeginString", "MsgType", "Symbol");
    }

    @Test
    void explainByOrderId_missingMessage_throwsNotFound() {
        Order order = OrderTestData.valid();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(fixMessageRepository.findByOrderId(order.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.explainByOrderId(order.getId()))
                .isInstanceOf(FixMessageNotFoundException.class)
                .hasMessageContaining(order.getId().toString());
    }

    @Test
    void explainRaw_validMessage_returnsTagExplanation() {
        List<FixTagExplanation> result = service.explainRaw("8=FIX.4.4|35=D");

        assertThat(result).extracting(FixTagExplanation::name)
                .containsExactly("BeginString", "MsgType");
    }

    @Test
    void explainRaw_invalidMessage_throwsInvalidFixMessage() {
        assertThatThrownBy(() -> service.explainRaw("invalid"))
                .isInstanceOf(InvalidFixMessageException.class)
                .hasMessageContaining("numericTag=value");
    }

    private FixMessage fixMessage(UUID orderId) {
        return new FixMessage(
                UUID.randomUUID(),
                orderId,
                "8=FIX.4.4|35=D|55=AAPL",
                FIXED_TIME,
                FIXED_TIME
        );
    }
}
