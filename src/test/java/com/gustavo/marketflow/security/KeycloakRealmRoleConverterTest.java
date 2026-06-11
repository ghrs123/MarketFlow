package com.gustavo.marketflow.security;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Test
    void convert_realmRoles_returnsRoleAuthoritiesAndScopes() {
        Jwt jwt = jwt(Map.of(
                "scope", "orders.read",
                "realm_access", Map.of("roles", List.of("TRADER", "admin"))
        ));

        assertThat(converter.convert(jwt))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("SCOPE_orders.read", "ROLE_TRADER", "ROLE_ADMIN");
    }

    @Test
    void convert_missingRealmAccess_returnsScopeAuthoritiesOnly() {
        Jwt jwt = jwt(Map.of("scope", "profile"));

        assertThat(converter.convert(jwt))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("SCOPE_profile");
    }

    private Jwt jwt(Map<String, Object> claims) {
        return new Jwt(
                "token-value",
                Instant.parse("2026-06-11T10:00:00Z"),
                Instant.parse("2026-06-11T10:05:00Z"),
                Map.of("alg", "RS256"),
                claims
        );
    }
}
