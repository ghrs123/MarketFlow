package com.gustavo.marketflow.order.api;

import com.gustavo.marketflow.order.domain.OrderSide;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Inbound DTO for {@code POST /orders}.
 *
 * <p>Implemented as a Java {@code record} for immutability and minimal
 * boilerplate. Bean Validation annotations are placed on the components
 * and triggered by {@code @Valid} in the controller, so any rule violation
 * surfaces as a 400 Bad Request handled by the global exception handler.</p>
 *
 * <p>Constraints capture the inbound contract:
 * <ul>
 *   <li>{@code clientId} / {@code symbol}: non-blank strings.</li>
 *   <li>{@code side}: required enum.</li>
 *   <li>{@code quantity} / {@code price}: positive {@link BigDecimal}
 *       with a bounded scale to reject inputs with absurd precision.</li>
 * </ul>
 */
public record CreateOrderRequest(

        @NotBlank(message = "clientId must not be blank")
        String clientId,

        @NotBlank(message = "symbol must not be blank")
        String symbol,

        @NotNull(message = "side must not be null")
        OrderSide side,

        @NotNull(message = "quantity must not be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "quantity must be positive")
        @Digits(integer = 18, fraction = 8, message = "quantity precision is too high")
        BigDecimal quantity,

        @NotNull(message = "price must not be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "price must be positive")
        @Digits(integer = 18, fraction = 8, message = "price precision is too high")
        BigDecimal price,

        @NotBlank(message = "idempotencyKey must not be blank")
        @Size(max = 128, message = "idempotencyKey must contain at most 128 characters")
        String idempotencyKey
) {
}
