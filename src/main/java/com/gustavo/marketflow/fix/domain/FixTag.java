package com.gustavo.marketflow.fix.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * Supported tag catalogue for the Phase 6 simulated FIX format.
 */
public enum FixTag {

    BEGIN_STRING("8", "BeginString", "Protocol version used by the simulated message"),
    MESSAGE_TYPE("35", "MsgType", "D identifies a simulated New Order Single"),
    SENDER_COMP_ID("49", "SenderCompID", "Identifier of the message sender"),
    TARGET_COMP_ID("56", "TargetCompID", "Identifier of the simulated broker"),
    CLIENT_ORDER_ID("11", "ClOrdID", "Client-provided order identity represented by the MarketFlow order id"),
    SYMBOL("55", "Symbol", "Instrument symbol"),
    SIDE("54", "Side", "1 means BUY and 2 means SELL"),
    ORDER_QUANTITY("38", "OrderQty", "Quantity requested by the order"),
    ORDER_TYPE("40", "OrdType", "2 identifies a limit order"),
    PRICE("44", "Price", "Limit price"),
    SENDING_TIME("52", "SendingTime", "UTC timestamp when the simulated message was generated");

    private final String number;
    private final String fieldName;
    private final String description;

    FixTag(String number, String fieldName, String description) {
        this.number = number;
        this.fieldName = fieldName;
        this.description = description;
    }

    public static Optional<FixTag> fromNumber(String number) {
        return Arrays.stream(values())
                .filter(tag -> tag.number.equals(number))
                .findFirst();
    }

    public String number() {
        return number;
    }

    public String fieldName() {
        return fieldName;
    }

    public String description() {
        return description;
    }
}
