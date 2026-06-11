# Phase 10 - Resilience Patterns

## What This Phase Delivers

Phase 10 adds Resilience4j to protect MarketFlow from simulated dependency
failures and local capacity exhaustion:

- Circuit Breaker on broker execution and market-data retrieval.
- Retry with exponential backoff on simulated FIX generation availability.
- Rate Limiter on order creation.
- Semaphore Bulkhead around asynchronous order processing.
- Graceful broker and market-data fallback responses.
- Resilience metrics through Actuator and Prometheus.

The external services remain in-process simulations. There is no Kafka,
Docker Compose or Kubernetes configuration in this phase.

The original curriculum describes this capability as Phase 11. The repository
delivers it as Phase 10 because security was completed as Phase 9 in the
implemented branch sequence.

## Architecture

```text
POST /orders
  -> OrderController @RateLimiter
  -> OrderApplicationService

POST /orders/{id}/execute-with-broker
  -> ExecutionController
  -> OrderExecutionService @CircuitBreaker
  -> BrokerClient
  -> NotificationClient
  -> BrokerExecutionResult or fallback result

POST /orders/{id}/fix-message
  -> FixMessageApplicationService @Retry
  -> FixGenerationAvailability
  -> FIX generation and persistence

ExecutionWorker
  -> OrderProcessingEngine
  -> Resilience4j Bulkhead
  -> transactional order transitions
```

## Failure Simulation

| Symbol | Behaviour |
| --- | --- |
| `BROKER_FAIL` | Broker call fails immediately and uses fallback |
| `BROKER_TIMEOUT` | Broker call waits, fails and uses fallback |
| `MARKET_FAIL` | Market data returns a stale fallback quote |
| `FIX_RETRY` | FIX dependency fails twice and succeeds on attempt three |
| `FIX_FAIL` | FIX dependency exhausts retry and returns HTTP 503 |

Normal market symbols keep the existing successful behaviour.

## Technical Decisions

- Annotation-based Circuit Breaker, Retry and Rate Limiter keep policy at the
  Spring-managed entry point where proxy interception is reliable.
- The processing Bulkhead is programmatic because workers invoke the engine
  directly; annotation self-invocation would silently bypass the Spring proxy.
- Broker calls run outside a database transaction so slow dependencies do not
  retain JDBC connections or database locks.
- Retry targets only `TransientExternalServiceException`. Validation, missing
  orders and duplicate FIX messages are not retried.
- Fallbacks expose explicit degraded state and never leak internal exceptions.

## Configuration

The defaults are in `application.yml` under `resilience4j` and
`marketflow.resilience`. Important settings:

- broker circuit breaker: count window `6`, minimum calls `3`, threshold `50%`
- FIX retry: `3` attempts, waits of `100ms` and `200ms`, multiplier `2`
- order rate limiter: `5` permits every `1s`, no wait
- processing bulkhead: `4` concurrent calls, no wait
- Actuator and Prometheus: management port `8081`

## How To Run

```bash
export DB_URL=jdbc:postgresql://localhost:5432/marketflow
export DB_USERNAME=marketflow
export DB_PASSWORD=marketflow
export KEYCLOAK_ISSUER_URI=http://localhost:8180/realms/marketflow
export KEYCLOAK_JWK_SET_URI=http://localhost:8180/realms/marketflow/protocol/openid-connect/certs

mvn spring-boot:run
```

## Curl Examples

```bash
curl -X POST "http://localhost:8080/orders/{id}/execute-with-broker" \
  -H "Authorization: Bearer $TOKEN"

curl "http://localhost:8080/external/market-data/AAPL" \
  -H "Authorization: Bearer $TOKEN"

curl "http://localhost:8080/external/market-data/MARKET_FAIL" \
  -H "Authorization: Bearer $TOKEN"

curl "http://localhost:8080/learning/resilience"
curl "http://localhost:8080/learning/circuit-breaker"
curl "http://localhost:8080/learning/rate-limit"
curl "http://localhost:8080/learning/bulkhead"

curl "http://localhost:8081/actuator/prometheus" \
  | grep resilience4j
```

## Metrics

Resilience4j publishes event and state metrics through:

```text
GET /actuator/metrics
GET /actuator/prometheus
```

Relevant Prometheus families include circuit breaker state and calls, retry
calls, rate limiter permissions and bulkhead concurrent-call capacity.

Examples:

```text
resilience4j_circuitbreaker_state
resilience4j_circuitbreaker_calls_seconds
resilience4j_retry_calls
resilience4j_ratelimiter_available_permissions
resilience4j_bulkhead_available_concurrent_calls
```

## How To Test

```bash
mvn test
mvn package

mvn -Dtest="ResiliencePatternsTest,ResilienceApplicationIntegrationTest,ExternalServiceControllerTest,ExecutionControllerTest,OrderProcessingEngineTest" test
```

The focused resilience suite verifies circuit opening, retry recovery, rate
limit exhaustion, bulkhead rejection, broker response mapping and market-data
responses.

Latest full-suite evidence:

```text
Tests run: 157, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Expected Logs

Successful broker calls record the order ID and generated broker reference.
Handled dependency failures use `WARN` and identify only the dependency class,
never an access token, request body or internal stack trace.

Representative messages:

```text
Order {orderId} accepted by simulated broker reference={brokerReference}
Broker fallback used for order {orderId} cause={exceptionType}
FIX generation retry exhausted for order {orderId} cause={exceptionType}
Market data fallback used for symbol={symbol} cause={exceptionType}
Order {orderId} rejected by processing bulkhead
```

## HTTP Failure Contract

| Scenario | HTTP/result |
| --- | --- |
| Order creation rate exceeded | `429` RFC 7807 `order-rate-limit` |
| FIX retries exhausted | `503` RFC 7807 `external-service-unavailable` |
| Broker unavailable | `200` degraded result with `fallback=true` |
| Market data unavailable | `200` stale quote with `fallback=true` |
| Missing order | Existing `404` domain error, not hidden by fallback |

## Interview Narrative

The phase distinguishes four failure-control goals. Circuit Breaker prevents
repeated calls to a dependency that is already unhealthy. Retry handles
transient errors, but only for an explicit exception type and with exponential
backoff to reduce retry storms.

Rate Limiter controls inbound demand over time, while Bulkhead controls
concurrent resource consumption. They solve different overload problems and
can be composed with a bounded queue for backpressure.

The processing bulkhead is programmatic by design. The worker owns a direct
reference to the engine, so relying on an annotation on a self-invoked method
would bypass Spring AOP. Applying the Resilience4j decorator at the actual
worker boundary makes the protection explicit and testable.

## Interview Questions

1. What causes a circuit breaker to move from CLOSED to OPEN?
2. Why should retries be limited to transient failures?
3. How does exponential backoff reduce a retry storm?
4. What is the difference between rate limiting and bulkheading?
5. Why can Spring AOP annotations fail under self-invocation?
6. Why should remote calls normally stay outside database transactions?
7. What information should a graceful fallback expose?
8. Which resilience metrics indicate dependency degradation?
9. How do a bounded queue and a bulkhead complement each other?
10. When would a time limiter be required in addition to a circuit breaker?

## Definition Of Done

- [x] Resilience4j Spring Boot starter configured
- [x] Broker Circuit Breaker and fallback implemented
- [x] FIX Retry with exponential backoff implemented
- [x] Order Rate Limiter implemented
- [x] Processing Bulkhead implemented
- [x] Learning endpoints implemented
- [x] Metrics exposed through Actuator and Prometheus
- [x] Unit and controller tests added
- [x] Spring AOP integration tests added
- [x] Full suite passes with 157 tests
- [x] README updated

## Suggested Commits

```text
build(resilience): add Resilience4j Spring Boot starter
feat(resilience): add simulated external service clients
feat(resilience): protect broker and FIX integrations
feat(resilience): add order rate limiter and processing bulkhead
feat(learning): add resilience pattern endpoints
test(resilience): cover fault tolerance patterns and fallbacks
docs(phase-10): document Resilience4j patterns
```
