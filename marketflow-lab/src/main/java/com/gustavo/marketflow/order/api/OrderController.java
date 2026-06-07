package com.gustavo.marketflow.order.api;

import com.gustavo.marketflow.order.application.OrderApplicationService;
import com.gustavo.marketflow.order.domain.Order;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST adapter for the order use cases.
 *
 * <p>Kept deliberately thin: the controller only performs input binding,
 * validation, DTO/domain mapping and HTTP status selection. All business
 * orchestration lives in {@link OrderApplicationService}.</p>
 *
 * <p>HTTP contract:
 * <ul>
 *   <li>{@code POST /orders} -&gt; 201 Created with {@code Location} header
 *       pointing to the new resource.</li>
 *   <li>{@code GET /orders/{id}} -&gt; 200 OK, or 404 via the global
 *       exception handler if the order does not exist.</li>
 *   <li>{@code GET /orders} -&gt; 200 OK with the list snapshot.</li>
 * </ul>
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        Order created = orderApplicationService.createOrder(
                request.clientId(),
                request.symbol(),
                request.side(),
                request.quantity(),
                request.price()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(OrderResponse.from(created));
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable UUID id) {
        return OrderResponse.from(orderApplicationService.findById(id));
    }

    @GetMapping
    public List<OrderResponse> findAll() {
        return orderApplicationService.findAll().stream()
                .map(OrderResponse::from)
                .toList();
    }
}
