package com.gustavo.marketflow.order.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Order aggregate root.
 *
 * <p>Identity is provided by {@link #id} which is a server-side generated
 * {@link UUID}. Identifier equality (rather than structural equality) is
 * used so that the same logical order keeps a stable identity across state
 * transitions (e.g. NEW -&gt; ACCEPTED).</p>
 *
 * <p>{@code quantity} and {@code price} are modelled with {@link BigDecimal}
 * to avoid binary floating-point rounding errors which are unacceptable in
 * any financial domain. {@code createdAt} and {@code updatedAt} are
 * {@link Instant} (UTC) to avoid time-zone ambiguity.</p>
 *
 * <p>The aggregate exposes a single mutating operation
 * ({@link #changeStatus(OrderStatus)}) so that state transitions are
 * centralized and {@code updatedAt} is always refreshed atomically with the
 * status change.</p>
 */
public final class Order {

    private final UUID id;
    private final String clientId;
    private final String symbol;
    private final OrderSide side;
    private final BigDecimal quantity;
    private final BigDecimal price;
    private final Instant createdAt;

    private OrderStatus status;
    private Instant updatedAt;

    public Order(UUID id,
                 String clientId,
                 String symbol,
                 OrderSide side,
                 BigDecimal quantity,
                 BigDecimal price,
                 OrderStatus status,
                 Instant createdAt,
                 Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.clientId = Objects.requireNonNull(clientId, "clientId");
        this.symbol = Objects.requireNonNull(symbol, "symbol");
        this.side = Objects.requireNonNull(side, "side");
        this.quantity = Objects.requireNonNull(quantity, "quantity");
        this.price = Objects.requireNonNull(price, "price");
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    /**
     * Factory used by the application layer when a new order is created.
     * Encapsulates the initial status ({@link OrderStatus#NEW}) and the
     * generation of identity and timestamps so callers cannot accidentally
     * create orders in an inconsistent state.
     */
    public static Order createNew(String clientId,
                                  String symbol,
                                  OrderSide side,
                                  BigDecimal quantity,
                                  BigDecimal price) {
        Instant now = Instant.now();
        return new Order(
                UUID.randomUUID(),
                clientId,
                symbol,
                side,
                quantity,
                price,
                OrderStatus.NEW,
                now,
                now
        );
    }

    public void changeStatus(OrderStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "newStatus");
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Identity-based equality: two orders are the same business entity if
     * and only if they share the same {@link #id}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
