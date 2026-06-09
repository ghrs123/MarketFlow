package com.gustavo.marketflow.learning.concurrency;

import java.util.List;

/**
 * Result snapshot for the CompletableFuture demo.
 */
public record CompletableFutureDemoResult(
        String result,
        List<String> threadNames,
        boolean mdcPropagated
) {
}
