package com.gustavo.marketflow.fix.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request contract for explaining a caller-provided simulated FIX message.
 */
public record RawFixMessageRequest(
        @NotBlank
        @Size(max = 4_096)
        String rawMessage
) {
}
