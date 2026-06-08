package com.gustavo.marketflow.monitoring.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight, application-owned health endpoint.
 *
 * <p>Coexists with Spring Boot Actuator's {@code /actuator/health}. The
 * Actuator endpoint is the production probe and aggregates indicator
 * results; this one is a simple, always-on summary that demonstrates how
 * the application can expose business-specific status information without
 * coupling clients to the Actuator format.</p>
 */
@RestController
@RequestMapping("/health")
public class CustomHealthController {

    @GetMapping("/custom")
    public Map<String, Object> custom() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", "marketflow-lab");
        body.put("phase", "02-persistence-jpa-transactions");
        body.put("timestamp", Instant.now().toString());
        return body;
    }
}
