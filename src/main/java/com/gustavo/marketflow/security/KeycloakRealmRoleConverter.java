package com.gustavo.marketflow.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

/**
 * Maps Keycloak realm roles to Spring Security role authorities while retaining OAuth scopes.
 */
@Component
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtGrantedAuthoritiesConverter scopeAuthoritiesConverter;

    public KeycloakRealmRoleConverter() {
        this.scopeAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>(scopeAuthoritiesConverter.convert(jwt));
        Object realmAccessClaim = jwt.getClaim(REALM_ACCESS_CLAIM);
        if (!(realmAccessClaim instanceof Map<?, ?> realmAccess)) {
            return List.copyOf(authorities);
        }

        Object rolesClaim = realmAccess.get(ROLES_CLAIM);
        if (!(rolesClaim instanceof Collection<?> roles)) {
            return List.copyOf(authorities);
        }

        roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(role -> !role.isBlank())
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase()))
                .forEach(authorities::add);
        return List.copyOf(authorities);
    }
}
