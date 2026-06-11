package com.gustavo.marketflow.fix.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderSide;

/**
 * Generates the deliberately simplified FIX-like String used by this lab.
 *
 * <p>This is not a real FIX encoder: it uses a printable pipe delimiter and
 * omits session, sequence, body-length and checksum semantics.</p>
 */
@Component
public class FixMessageGenerator {

    private static final String BEGIN_STRING = "FIX.4.4";
    private static final String MESSAGE_TYPE_NEW_ORDER_SINGLE = "D";
    private static final String SENDER = "MARKETFLOW";
    private static final String TARGET = "SIMULATED_BROKER";
    private static final String LIMIT_ORDER_TYPE = "2";

    private final Clock clock;

    public FixMessageGenerator(Clock clock) {
        this.clock = clock;
    }

    public String generate(Order order) {
        return String.join("|",
                "8=" + BEGIN_STRING,
                "35=" + MESSAGE_TYPE_NEW_ORDER_SINGLE,
                "49=" + SENDER,
                "56=" + TARGET,
                "11=" + order.getId(),
                "55=" + order.getSymbol(),
                "54=" + sideCode(order.getSide()),
                "38=" + formatDecimal(order.getQuantity()),
                "40=" + LIMIT_ORDER_TYPE,
                "44=" + formatDecimal(order.getPrice()),
                "52=" + DateTimeFormatter.ISO_INSTANT.format(clock.instant())
        );
    }

    private String sideCode(OrderSide side) {
        return switch (side) {
            case BUY -> "1";
            case SELL -> "2";
        };
    }

    private String formatDecimal(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
