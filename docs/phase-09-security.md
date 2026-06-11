# Phase 9 - JWT Security with Keycloak

## What this phase delivers

MarketFlow is now a stateless OAuth2 Resource Server. Protected endpoints
require Keycloak access tokens, realm roles are mapped to Spring Security
authorities, administrative operations are isolated and authentication context
is included in structured logs without exposing tokens.

The curriculum currently labels this capability as Phase 10. The project
instruction deliberately delivers it as Phase 9 after observability, so the
branch is named `feature/09-security`.

## Architecture

1. `CorrelationIdFilter` executes before Spring Security and establishes the
   correlation context for successful and rejected requests.
2. Spring Security reads the bearer token and validates its signature, issuer
   and standard time claims using Keycloak public keys.
3. `KeycloakRealmRoleConverter` maps `realm_access.roles` to `ROLE_TRADER` and
   `ROLE_ADMIN`, while retaining OAuth scope authorities.
4. URL authorization rejects unauthorized requests before controller dispatch.
5. `@PreAuthorize` repeats critical role rules at the adapter boundary.
6. `AuthenticatedUserMdcFilter` adds the JWT subject to MDC as `userId` and
   restores the previous context after the request.
7. Authentication and authorization failures use correlated RFC 7807
   responses and never expose token contents or internal exception messages.

## Authorization matrix

| Resource | Required authority |
| --- | --- |
| `/orders/**`, `/order-book/**`, `/fix/**` | `ROLE_TRADER` or `ROLE_ADMIN` |
| `POST /orders/{id}/queue` | `ROLE_TRADER` or `ROLE_ADMIN` |
| `/execution/**`, `/events/**` | `ROLE_ADMIN` |
| `/monitoring/**`, detailed Actuator metrics | `ROLE_ADMIN` |
| `/learning/**`, OpenAPI and health probes | Public |

## Keycloak setup

Import `keycloak/marketflow-realm.json` into a Keycloak instance. The import
creates:

- Realm `marketflow`.
- Realm roles `TRADER` and `ADMIN`.
- Public development client `marketflow-cli`.

Create users in Keycloak and assign the required realm roles. No users,
passwords or client secrets are stored in this repository.

Configure the application:

```bash
export KEYCLOAK_ISSUER_URI=http://localhost:8180/realms/marketflow
export KEYCLOAK_JWK_SET_URI=http://localhost:8180/realms/marketflow/protocol/openid-connect/certs
```

The explicit JWK endpoint avoids identity-provider discovery during application
startup. JWT issuer validation remains active.

## Obtain a development token

```bash
export KEYCLOAK_USERNAME=your-user
export KEYCLOAK_PASSWORD=your-password

export TOKEN=$(
  curl -s -X POST \
    http://localhost:8180/realms/marketflow/protocol/openid-connect/token \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d client_id=marketflow-cli \
    -d username="$KEYCLOAK_USERNAME" \
    -d password="$KEYCLOAK_PASSWORD" \
    -d grant_type=password |
  jq -r .access_token
)
```

Password grant is enabled only for the CLI learning flow. Browser applications
should use Authorization Code with PKCE.

## Demonstrate

```bash
# Public
curl -i http://localhost:8080/learning/security

# 401 without a token
curl -i http://localhost:8080/orders

# TRADER or ADMIN
curl -i http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN"

# ADMIN only
curl -i http://localhost:8080/monitoring/summary \
  -H "Authorization: Bearer $TOKEN"
```

## Security decisions and trade-offs

- Sessions are disabled because the service authenticates every request using
  a bearer token.
- CSRF is disabled because credentials are not automatically attached by
  browser cookies. Cookie-based authentication would require CSRF protection.
- CORS remains closed by default. A future browser client must define explicit
  trusted origins rather than use a wildcard policy.
- Both URL and method authorization are used. This duplicates a small amount of
  policy but protects critical methods if URL routing changes.
- Health status remains public for probes, while health details and operational
  metrics require `ADMIN`.
- The resource server never receives user passwords; Keycloak owns
  authentication and token issuance.

## Test

```bash
mvn test
mvn package
```

Security tests cover:

- Public access.
- Missing token returning 401.
- Insufficient role returning 403.
- `TRADER` access to order resources.
- `ADMIN` access to monitoring resources.
- Method security with `@WithMockUser`.
- Keycloak realm-role conversion.
- MDC `userId` propagation and restoration.

## Interview narrative

MarketFlow separates identity management from API authorization. Keycloak
authenticates users and issues signed JWT access tokens; MarketFlow acts only as
a resource server and validates those tokens using the realm issuer and public
JWK set.

Authorization uses realm roles because they represent application-wide
capabilities. The converter maps Keycloak roles to Spring's `ROLE_*`
authorities, and critical endpoints are protected both in the HTTP filter chain
and with method security.

Observability remains intact after security is enabled. Correlation ID is
established before authentication, so even 401 and 403 responses can be traced.
After authentication, the JWT subject is added to MDC as `userId`, while tokens
and claims containing sensitive information are never logged.

## Interview questions

1. What is the difference between an OAuth2 authorization server and resource server?
2. How does a resource server validate a JWT without calling Keycloak per request?
3. What is the difference between roles and authorities in Spring Security?
4. Why map Keycloak realm roles instead of relying only on scopes?
5. Why is stateless authentication appropriate for this API?
6. When is disabling CSRF safe, and when is it dangerous?
7. What is the difference between 401 and 403?
8. Why use both request authorization and `@PreAuthorize`?
9. How do you avoid leaking bearer tokens through logs?
10. How would token revocation affect a self-contained JWT design?

## Exercises

1. Replace the CLI password grant with Authorization Code and PKCE.
2. Add audience validation for a dedicated MarketFlow API audience.
3. Configure explicit CORS origins for a browser client.
4. Add client roles and compare them with realm roles.
5. Add automated tests for expired and wrong-issuer JWTs using a local decoder.

## Definition of done

- [x] Spring Security and OAuth2 Resource Server dependencies exist.
- [x] Keycloak issuer and JWK endpoints are environment-configurable.
- [x] Protected endpoints reject missing and insufficient credentials.
- [x] `TRADER` and `ADMIN` role policies are enforced.
- [x] Correlation ID remains available for 401 and 403 responses.
- [x] Authenticated subject is propagated through MDC.
- [x] Security learning endpoints and Keycloak setup are documented.
- [x] Full Maven test suite passes (147 tests, 0 failures, 0 errors).
