package com.gustavo.marketflow.fix.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gustavo.marketflow.fix.domain.FixMessage;
import com.gustavo.marketflow.fix.domain.FixMessageRepository;
import com.gustavo.marketflow.fix.infrastructure.jpa.SpringDataFixMessageJpaRepository;
import com.gustavo.marketflow.order.OrderTestData;
import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderRepository;
import com.gustavo.marketflow.order.infrastructure.jpa.SpringDataOrderHistoryJpaRepository;
import com.gustavo.marketflow.order.infrastructure.jpa.SpringDataOrderJpaRepository;
import com.gustavo.marketflow.shared.exception.FixMessageAlreadyExistsException;
import com.gustavo.marketflow.support.PostgreSqlContainerBaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class FixMessageRepositoryIntegrationTest extends PostgreSqlContainerBaseTest {

    private final FixMessageRepository fixMessageRepository;
    private final OrderRepository orderRepository;
    private final SpringDataFixMessageJpaRepository springDataFixMessageJpaRepository;
    private final SpringDataOrderHistoryJpaRepository springDataOrderHistoryJpaRepository;
    private final SpringDataOrderJpaRepository springDataOrderJpaRepository;

    @Autowired
    FixMessageRepositoryIntegrationTest(FixMessageRepository fixMessageRepository,
                                        OrderRepository orderRepository,
                                        SpringDataFixMessageJpaRepository springDataFixMessageJpaRepository,
                                        SpringDataOrderHistoryJpaRepository springDataOrderHistoryJpaRepository,
                                        SpringDataOrderJpaRepository springDataOrderJpaRepository) {
        this.fixMessageRepository = fixMessageRepository;
        this.orderRepository = orderRepository;
        this.springDataFixMessageJpaRepository = springDataFixMessageJpaRepository;
        this.springDataOrderHistoryJpaRepository = springDataOrderHistoryJpaRepository;
        this.springDataOrderJpaRepository = springDataOrderJpaRepository;
    }

    @BeforeEach
    void setUp() {
        springDataFixMessageJpaRepository.deleteAll();
        springDataOrderHistoryJpaRepository.deleteAll();
        springDataOrderJpaRepository.deleteAll();
    }

    @Test
    void saveAndFindByOrderId_persistsMessage() {
        Order order = orderRepository.save(OrderTestData.valid());
        FixMessage fixMessage = fixMessage(order.getId());

        FixMessage saved = fixMessageRepository.save(fixMessage);

        assertThat(fixMessageRepository.findByOrderId(order.getId())).contains(saved);
        assertThat(fixMessageRepository.existsByOrderId(order.getId())).isTrue();
    }

    @Test
    void save_secondMessageForSameOrder_throwsConflict() {
        Order order = orderRepository.save(OrderTestData.valid());
        fixMessageRepository.save(fixMessage(order.getId()));

        assertThatThrownBy(() -> fixMessageRepository.save(fixMessage(order.getId())))
                .isInstanceOf(FixMessageAlreadyExistsException.class)
                .hasMessageContaining(order.getId().toString());
    }

    private FixMessage fixMessage(UUID orderId) {
        Instant now = Instant.parse("2026-01-15T10:30:00Z");
        return new FixMessage(
                UUID.randomUUID(),
                orderId,
                "8=FIX.4.4|35=D|55=AAPL",
                now,
                now
        );
    }
}
