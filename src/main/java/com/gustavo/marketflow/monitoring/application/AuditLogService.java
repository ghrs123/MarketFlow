package com.gustavo.marketflow.monitoring.application;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.gustavo.marketflow.shared.logging.MdcContext;

/**
 * Emits business audit records through a dedicated logger without exposing sensitive payloads.
 */
@Service
public class AuditLogService {

    private static final Logger auditLog = LoggerFactory.getLogger("MARKETFLOW_AUDIT");

    public void recordOrderEvent(String action, UUID orderId, String outcome) {
        Map<String, String> context = new HashMap<>();
        Map<String, String> currentContext = MDC.getCopyOfContextMap();
        if (currentContext != null) {
            context.putAll(currentContext);
        }
        context.put("orderId", orderId.toString());
        MdcContext.runWith(context,
                () -> auditLog.info("Order audit action={} outcome={}", action, outcome));
    }
}
