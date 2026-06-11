package com.gustavo.marketflow.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Defines the public OpenAPI metadata for the MarketFlow REST API.
 *
 * <p>The generated contract remains derived from controller mappings and
 * DTOs. Centralising only the service-level metadata avoids duplicating the
 * executable HTTP contract in a manually maintained specification.</p>
 */
@Configuration
public class OpenApiConfiguration {

    /**
     * Builds the root OpenAPI document metadata displayed by Swagger UI.
     *
     * @return OpenAPI metadata shared by all documented endpoints
     */
    @Bean
    public OpenAPI marketFlowOpenApi() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Keycloak access token")))
                .info(new Info()
                        .title("MarketFlow Order and FIX API")
                        .description("""
                                REST API for market orders, concurrent processing and simulated FIX messages.
                                Protected endpoints require a Keycloak JWT with TRADER or ADMIN realm roles.
                                Error responses follow RFC 7807 Problem Details.
                                """)
                        .version("9.0.0")
                        .contact(new Contact()
                                .name("MarketFlow Engineering"))
                        .license(new License()
                                .name("Educational project")));
    }
}
