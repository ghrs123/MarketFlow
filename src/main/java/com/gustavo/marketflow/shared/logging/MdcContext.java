package com.gustavo.marketflow.shared.logging;

import java.util.Map;

import org.slf4j.MDC;

/**
 * Centralizes MDC restoration so asynchronous components cannot leak context between reused threads.
 */
public final class MdcContext {

    private MdcContext() {
    }

    public static void runWith(Map<String, String> contextMap, Runnable action) {
        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        try {
            set(contextMap);
            action.run();
        } finally {
            set(previousContext);
        }
    }

    public static void set(Map<String, String> contextMap) {
        if (contextMap == null || contextMap.isEmpty()) {
            MDC.clear();
            return;
        }
        MDC.setContextMap(contextMap);
    }
}
