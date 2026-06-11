package com.gustavo.marketflow.security;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.gustavo.marketflow.support.PostgreSqlContainerBaseTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the production security chain with mocked JWTs and method security enabled.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest extends PostgreSqlContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    private final KeycloakRealmRoleConverter roleConverter = new KeycloakRealmRoleConverter();

    @Test
    void learningEndpoint_withoutAuthentication_returnsOk() throws Exception {
        mockMvc.perform(get("/learning/security"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("Spring Security resource server"));
    }

    @Test
    void health_withoutAuthentication_returnsOk() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void orders_withoutAuthentication_returnsCorrelatedProblemDetail() throws Exception {
        mockMvc.perform(get("/orders")
                        .header("X-Correlation-Id", "security-test-401"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string("X-Correlation-Id", "security-test-401"))
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.title").value("Authentication required"))
                .andExpect(jsonPath("$.correlationId").value("security-test-401"));
    }

    @Test
    void orders_authenticatedWithoutRole_returnsForbidden() throws Exception {
        mockMvc.perform(get("/orders")
                        .with(jwtWithRealmRoles("observer")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Access denied"));
    }

    @Test
    void orders_traderRealmRole_returnsOk() throws Exception {
        mockMvc.perform(get("/orders")
                        .with(jwtWithRealmRoles("trader-1", "TRADER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void monitoring_traderRealmRole_returnsForbidden() throws Exception {
        mockMvc.perform(get("/monitoring/summary")
                        .with(jwtWithRealmRoles("trader-1", "TRADER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void monitoring_adminRealmRole_returnsOk() throws Exception {
        mockMvc.perform(get("/monitoring/summary")
                        .with(jwtWithRealmRoles("admin-1", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("marketflow-lab"));
    }

    @Test
    void actuatorInfo_traderRealmRole_returnsForbidden() throws Exception {
        mockMvc.perform(get("/actuator/info")
                        .with(jwtWithRealmRoles("trader-1", "TRADER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void actuatorInfo_adminRealmRole_returnsOk() throws Exception {
        mockMvc.perform(get("/actuator/info")
                        .with(jwtWithRealmRoles("admin-1", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-trader", roles = "TRADER")
    void orders_mockTraderUser_methodSecurityAllowsAccess() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk());
    }

    private RequestPostProcessor jwtWithRealmRoles(String subject, String... roles) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .issuer("http://localhost:8180/realms/marketflow")
                .subject(subject)
                .issuedAt(Instant.parse("2026-06-11T10:00:00Z"))
                .expiresAt(Instant.parse("2026-06-11T10:05:00Z"))
                .claim("realm_access", Map.of("roles", List.of(roles)))
                .build();
        return authentication(new JwtAuthenticationToken(
                jwt,
                roleConverter.convert(jwt),
                subject
        ));
    }
}
