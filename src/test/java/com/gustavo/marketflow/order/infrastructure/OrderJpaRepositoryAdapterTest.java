package com.gustavo.marketflow.order.infrastructure;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderRepository;
import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.order.domain.OrderStatus;
import com.gustavo.marketflow.order.infrastructure.jpa.SpringDataOrderHistoryJpaRepository;
import com.gustavo.marketflow.order.infrastructure.jpa.SpringDataOrderJpaRepository;
import com.gustavo.marketflow.support.PostgreSqlContainerBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderJpaRepositoryAdapterTest extends PostgreSqlContainerBaseTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SpringDataOrderJpaRepository springDataOrderJpaRepository;

    @Autowired
    private SpringDataOrderHistoryJpaRepository springDataOrderHistoryJpaRepository;

    @BeforeEach
    void setUp() {
        springDataOrderHistoryJpaRepository.deleteAll();
        springDataOrderJpaRepository.deleteAll();
    }

    @Test
    void saveAndFindById_persistsToPostgreSql() {
        Order created = Order.createNew(
                "C001",
                "AAPL",
                OrderSide.BUY,
                new BigDecimal("10"),
                new BigDecimal("150.25")
        );

        orderRepository.save(created);

        assertThat(orderRepository.findById(created.getId())).isPresent();
    }

    @Test
    void findByFilters_appliesClientStatusAndPagination() {
        orderRepository.save(Order.createNew("C001", "AAPL", OrderSide.BUY, new BigDecimal("1"), new BigDecimal("10")));
        orderRepository.save(Order.createNew("C001", "MSFT", OrderSide.BUY, new BigDecimal("1"), new BigDecimal("11")));
        orderRepository.save(Order.createNew("C002", "GOOG", OrderSide.BUY, new BigDecimal("1"), new BigDecimal("12")));

        assertThat(orderRepository.findByFilters("C001", OrderStatus.NEW, 0, 1)).hasSize(1);
        assertThat(orderRepository.countByFilters("C001", OrderStatus.NEW)).isEqualTo(2);
    }
}
