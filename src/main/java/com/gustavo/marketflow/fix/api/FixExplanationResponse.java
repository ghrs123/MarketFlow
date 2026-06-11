package com.gustavo.marketflow.fix.api;

import java.util.List;
import java.util.UUID;

import com.gustavo.marketflow.fix.domain.FixTagExplanation;

/**
 * Public explanation of all tags contained in a simulated FIX message.
 */
public record FixExplanationResponse(
        UUID orderId,
        List<FixTagExplanationResponse> tags
) {

    public static FixExplanationResponse from(UUID orderId, List<FixTagExplanation> explanations) {
        return new FixExplanationResponse(
                orderId,
                explanations.stream()
                        .map(FixTagExplanationResponse::from)
                        .toList()
        );
    }
}
