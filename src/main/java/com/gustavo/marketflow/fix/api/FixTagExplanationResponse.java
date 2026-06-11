package com.gustavo.marketflow.fix.api;

import com.gustavo.marketflow.fix.domain.FixTagExplanation;

/**
 * Public representation of one explained FIX tag.
 */
public record FixTagExplanationResponse(
        String tag,
        String name,
        String value,
        String description,
        boolean known
) {

    public static FixTagExplanationResponse from(FixTagExplanation explanation) {
        return new FixTagExplanationResponse(
                explanation.tag(),
                explanation.name(),
                explanation.value(),
                explanation.description(),
                explanation.known()
        );
    }
}
