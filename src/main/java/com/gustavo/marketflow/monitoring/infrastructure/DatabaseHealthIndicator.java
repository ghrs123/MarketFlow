package com.gustavo.marketflow.monitoring.infrastructure;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Verifies that the configured database can accept a connection without exposing connection details.
 */
@Component("databaseHealthIndicator")
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final int VALIDATION_TIMEOUT_SECONDS = 2;

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(VALIDATION_TIMEOUT_SECONDS)) {
                return Health.up()
                        .withDetail("dependency", "database")
                        .build();
            }
            return Health.down()
                    .withDetail("dependency", "database")
                    .build();
        } catch (SQLException ex) {
            return Health.down()
                    .withDetail("dependency", "database")
                    .build();
        }
    }
}
