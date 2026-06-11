package com.gustavo.marketflow.shared.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides time infrastructure for deterministic FIX generation tests.
 */
@Configuration
public class FixConfiguration {

    @Bean
    public Clock utcClock() {
        return Clock.systemUTC();
    }
}
