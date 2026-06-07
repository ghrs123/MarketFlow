package com.gustavo.marketflow.order.application;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderRepository;
import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.shared.exception.OrderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Application service that orchestrates order use cases.
 *
 * <p>Sits between the inbound REST adapter and the outbound persistence
 * port. Holds no state of its own and depends on
 * {@link OrderRepository} through constructor injection, which makes the
 * collaborator explicit, the class trivially testable with a fake
 * repository and the dependency graph validated at startup.</p>
 *
 * <p>The service deliberately speaks the domain language (it returns
 * {@link Order} aggregates) and never deals with HTTP, DTOs or
 * serialization. That mapping lives in the API layer.</p>
 */
@Service
public class OrderApplicationService {

    private static final Logger log = LoggerFactory.getLogger(OrderApplicationService.class);

    private final OrderRepository orderRepository;

    public OrderApplicationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(String clientId,
                             String symbol,
                             OrderSide side,
                             BigDecimal quantity,
                             BigDecimal price) {
        Order order = Order.createNew(clientId, symbol, side, quantity, price);
        Order saved = orderRepository.save(order);
        log.info("Order created id={} clientId={} symbol={} side={} qty={} price={}",
                saved.getId(), saved.getClientId(), saved.getSymbol(),
                saved.getSide(), saved.getQuantity(), saved.getPrice());
        return saved;
    }

    public Order findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }
}
