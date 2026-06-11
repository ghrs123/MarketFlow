package com.gustavo.marketflow.shared.config;

import com.gustavo.marketflow.resilience.application.ResilienceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers validated configuration used by simulated external dependencies.
 */
@Configuration
@EnableConfigurationProperties(ResilienceProperties.class)
public class ResilienceConfiguration {
}
