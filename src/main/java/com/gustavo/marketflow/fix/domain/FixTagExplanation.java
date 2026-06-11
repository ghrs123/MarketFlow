package com.gustavo.marketflow.fix.domain;

/**
 * Explanation of one parsed tag from a simulated FIX message.
 */
public record FixTagExplanation(
        String tag,
        String name,
        String value,
        String description,
        boolean known
) {
}
