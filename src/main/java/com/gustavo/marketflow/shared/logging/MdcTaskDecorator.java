package com.gustavo.marketflow.shared.logging;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

/**
 * Captures the submitting thread MDC and restores it around execution on a pooled thread.
 */
@Component
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> capturedContext = MDC.getCopyOfContextMap();
        return () -> MdcContext.runWith(capturedContext, runnable);
    }
}
