package com.gustavo.marketflow.order.domain;

import java.util.List;

/**
 * Immutable page result in domain language to avoid leaking framework
 * pagination types across architectural boundaries.
 */
public record OrderPage(
        List<Order> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
