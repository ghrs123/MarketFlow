package com.gustavo.marketflow.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures MarketFlow as a stateless OAuth2 resource server backed by Keycloak JWTs.
 *
 * <p>CSRF is disabled because authentication is supplied exclusively through
 * bearer tokens rather than browser-managed cookies. Introducing cookie-based
 * authentication would require revisiting this decision.</p>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    private final KeycloakRealmRoleConverter realmRoleConverter;
    private final BearerAuthenticationEntryPoint authenticationEntryPoint;
    private final BearerAccessDeniedHandler accessDeniedHandler;

    public SecurityConfiguration(KeycloakRealmRoleConverter realmRoleConverter,
                                 BearerAuthenticationEntryPoint authenticationEntryPoint,
                                 BearerAccessDeniedHandler accessDeniedHandler) {
        this.realmRoleConverter = realmRoleConverter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/error",
                                "/learning/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/health/custom",
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()
                        .requestMatchers(
                                "/monitoring/**",
                                "/events/**",
                                "/execution/**",
                                "/actuator/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "/orders/**",
                                "/order-book/**",
                                "/fix/**"
                        ).hasAnyRole("TRADER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .addFilterAfter(new AuthenticatedUserMdcFilter(), BearerTokenAuthenticationFilter.class)
                .build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(realmRoleConverter);
        return converter;
    }
}
