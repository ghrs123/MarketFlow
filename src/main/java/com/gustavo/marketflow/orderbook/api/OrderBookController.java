package com.gustavo.marketflow.orderbook.api;

import com.gustavo.marketflow.orderbook.application.OrderBookApplicationService;
import com.gustavo.marketflow.orderbook.application.OrderBookSnapshot;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST adapter for the in-memory order-book use cases.
 *
 * <p>The controller exposes a read/write API for the order book while keeping
 * all ordering and cache rules inside the application and domain layers.</p>
 */
@RestController
@RequestMapping
public class OrderBookController {

    private final OrderBookApplicationService orderBookApplicationService;

    public OrderBookController(OrderBookApplicationService orderBookApplicationService) {
        this.orderBookApplicationService = orderBookApplicationService;
    }

    @PostMapping("/orders/{id}/book")
    public ResponseEntity<Void> addToBook(@PathVariable UUID id) {
        orderBookApplicationService.addToBook(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/order-book")
    public OrderBookSnapshotResponse getSnapshot() {
        OrderBookSnapshot snapshot = orderBookApplicationService.getSnapshot();
        return OrderBookSnapshotResponse.from(
                snapshot.buyOrders(),
                snapshot.sellOrders(),
                snapshot.timestamp()
        );
    }

    @GetMapping("/order-book/best-buy")
    public OrderTaskResponse getBestBuy() {
        return OrderTaskResponse.from(orderBookApplicationService.getBestBuy());
    }

    @GetMapping("/order-book/best-sell")
    public OrderTaskResponse getBestSell() {
        return OrderTaskResponse.from(orderBookApplicationService.getBestSell());
    }

    @GetMapping("/order-book/recent/{id}")
    public OrderTaskResponse getRecent(@PathVariable UUID id) {
        return OrderTaskResponse.from(orderBookApplicationService.getRecent(id));
    }
}
