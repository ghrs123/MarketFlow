package com.gustavo.marketflow.order.domain;

import java.util.List;
import java.util.UUID;

/**
 * Outbound port for persisting and querying order history events.
 */
public interface OrderHistoryRepository {

    OrderHistory save(OrderHistory orderHistory);

    List<OrderHistory> findByOrderId(UUID orderId);
}
