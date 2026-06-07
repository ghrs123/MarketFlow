package com.gustavo.marketflow.order.api;

import com.gustavo.marketflow.order.domain.OrderPage;

import java.util.List;

/**
 * API contract for paginated order listing.
 */
public record OrderPageResponse(
        List<OrderResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static OrderPageResponse from(OrderPage orderPage) {
        return new OrderPageResponse(
                orderPage.content().stream().map(OrderResponse::from).toList(),
                orderPage.page(),
                orderPage.size(),
                orderPage.totalElements(),
                orderPage.totalPages()
        );
    }
}
