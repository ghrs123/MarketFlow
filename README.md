# MarketFlow Senior Java Cloud Lab

> Hands-on study lab for Senior Java/Spring Boot interviews, structured as
> an incremental course. Each phase ships a complete vertical slice with
> its own branch, tests and documentation.

This repository is the implementation of Phase 10 of the
`MarketFlow Senior Java Cloud Lab` curriculum.

## Current phase

**Phase 10 - Resilience patterns with Resilience4j**

This phase protects simulated dependencies and processing capacity with
Circuit Breaker, Retry, Rate Limiter and Bulkhead while retaining JWT security,
correlated observability and Prometheus metrics.

## Stack

- Java 21
- Spring Boot 3.3.5 (Web, Validation, Actuator, Data JPA, Security)
- OAuth2 Resource Server + JWT + Keycloak
- Resilience4j Circuit Breaker, Retry, Rate Limiter and Bulkhead
- Micrometer + Prometheus registry
- Maven
- PostgreSQL + Flyway
- JUnit 5 + Mockito + MockMvc + AssertJ + Testcontainers + JaCoCo
- Simulated FIX messages without an external FIX library
- Internal event-driven processing without Kafka or RabbitMQ
- In-memory data structures: `PriorityQueue`, `LinkedHashMap`, `ConcurrentHashMap`, `BlockingQueue`
- Concurrency primitives: `ExecutorService`, `CompletableFuture`, `AtomicLong`, `ReentrantLock`

## Project layout

```text
src/main/java/com/gustavo/marketflow
|- event
|  |- api              # Published-event query endpoints
|  |- domain           # Immutable business event contracts
|  `- infrastructure   # Process-local event bus
|- fix
|  |- api              # FIX generation, lookup and explanation endpoints
|  |- application      # Generator, parser, explainer and orchestration
|  |- domain           # FIX message, tag catalogue and persistence port
|  `- infrastructure   # PostgreSQL adapter
|- execution
|  |- api              # Queue and worker lifecycle endpoints
|  |- application      # Processing engine, workers, transactional execution service
|  `- domain           # BlockingQueue wrapper and queue payloads
|- learning
|  |- concurrency      # Race condition, locks, deadlock and CompletableFuture demos
|  `- LearningController.java
|- order
|  |- api
|  |- application
|  |- domain
|  `- infrastructure
|- orderbook
|  |- api
|  |- application
|  `- domain
|- monitoring          # Operational summary, metrics and health indicators
|- resilience          # Simulated dependencies and fault-tolerance contracts
|- security            # JWT conversion, authorization and security errors
`- shared
   |- config
   |- exception
   `- logging
```

## Prerequisites

| Tool | Version | Purpose |
|---|---|---|
| JDK | 21 | Runtime |
| Maven | 3.9+ | Build |
| PostgreSQL | 14+ | Persistence |
| Keycloak | 24+ | Identity provider |
| Docker | any | Testcontainers + optional monitoring |

## How to run

### 1. Start PostgreSQL

```bash
docker run -d --name marketflow-db \
  -e POSTGRES_DB=marketflow \
  -e POSTGRES_USER=marketflow \
  -e POSTGRES_PASSWORD=marketflow \
  -p 5433:5432 \
  postgres:16-alpine
```

### 2. Start Keycloak

```bash
docker run -d --name keycloak \
  -p 8180:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:24.0.0 \
  start-dev
```

Import the realm:
1. Open `http://localhost:8180` → login `admin/admin`
2. Create realm → Import → select `keycloak/marketflow-realm.json`
3. Create users with roles `TRADER` and `ADMIN` in the realm

### 3. Start the application

```bash
export DB_URL=jdbc:postgresql://localhost:5433/marketflow
export DB_USERNAME=marketflow
export DB_PASSWORD=marketflow
export KEYCLOAK_ISSUER_URI=http://localhost:8180/realms/marketflow
export KEYCLOAK_JWK_SET_URI=http://localhost:8180/realms/marketflow/protocol/openid-connect/certs

mvn spring-boot:run
```

- API: `http://localhost:8080`
- Actuator / metrics: `http://localhost:8081/actuator`

### 4. Obtain a token

```bash
TOKEN=$(curl -s -X POST \
  http://localhost:8180/realms/marketflow/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=marketflow-cli&username=admin1&password=admin1" \
  | jq -r '.access_token')
```

## How to test

Run the full suite:

```bash
mvn test
```

Run tests by phase:

```bash
# Phase 1-2: orders and persistence
mvn -Dtest="OrderApplicationServiceTest,OrderControllerTest,OrderRepositoryIntegrationTest" test

# Phase 4: order book and data structures
mvn -Dtest="OrderBookApplicationServiceTest,OrderBookControllerTest,OrderBookTest,RecentOrderCacheTest" test

# Phase 5: concurrency and processing engine
mvn -Dtest="OrderProcessingEngineTest,ExecutionControllerTest,ExecutionStatisticsTest" test

# Phase 6: FIX message engine
mvn -Dtest="FixMessageGeneratorTest,FixMessageParserTest,FixControllerTest,FixMessageRepositoryIntegrationTest" test

# Phase 7: event-driven, retry, DLQ, idempotency
mvn -Dtest="InMemoryEventBusTest,EventControllerTest,RetryRegistryTest,DeadLetterQueueTest,IdempotencyRegistryTest" test

# Phase 8: observability
mvn -Dtest="CorrelationIdFilterTest,MdcTaskDecoratorTest,OrderMetricsServiceTest,MonitoringSummaryControllerTest" test

# Phase 9: security
mvn -Dtest="SecurityIntegrationTest,KeycloakRealmRoleConverterTest,AuthenticatedUserMdcFilterTest" test

# Phase 10: resilience
mvn -Dtest="ResiliencePatternsTest,ResilienceApplicationIntegrationTest,ExternalServiceControllerTest,ExecutionControllerTest,OrderProcessingEngineTest" test
```

Latest verified result:

```text
Tests run: 157, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Generate JaCoCo coverage report:

```bash
mvn verify
# Report: target/site/jacoco/index.html
```

Note: Docker is required locally for Testcontainers-backed integration tests.

## Monitoring (optional)

Start Prometheus and Grafana:

```bash
docker compose -f docker-compose-monitoring.yml up -d
```

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` → admin/admin

The Actuator runs on port 8081 (no authentication — isolated from the API port).
Prometheus scrapes `http://host.docker.internal:8081/actuator/prometheus`.

Import `observability/grafana/marketflow-dashboard.json` to see:
- Orders created and processing rate
- Order queue size and DLQ size
- Processing latency p95
- HTTP request rate by status
- Circuit breaker state
- Rate limiter available permissions

## Security model

All API endpoints on port 8080 require a valid JWT bearer token issued by Keycloak.

| Role | Access |
|---|---|
| `TRADER` | `/orders/**`, `/order-book/**`, `/fix/**`, `/external/**` |
| `ADMIN` | All above + `/monitoring/**`, `/events/**`, `/execution/**` |
| Public | `/learning/**`, `/swagger-ui.html`, `/v3/api-docs/**`, `/health/custom`, `/actuator/health` |

Port 8081 (Actuator/metrics) is public — protect at network level in production.

## Resilience patterns (Phase 10)

| Pattern | Where | Config |
|---|---|---|
| Circuit Breaker | `OrderExecutionService` (broker calls) | 50% failure threshold, 5s open window |
| Retry | `FixMessageApplicationService` | 3 attempts, exponential waits of 100ms and 200ms |
| Rate Limiter | `OrderController` (POST /orders) | 5 requests/second |
| Bulkhead | Order processing | 4 concurrent calls max |

## Endpoints

| Method | Path | Role | Purpose |
|---|---|---|---|
| POST | `/orders` | TRADER | Create a new persisted order |
| GET | `/orders/{id}` | TRADER | Fetch one order by id |
| GET | `/orders` | TRADER | Filter and paginate orders |
| GET | `/orders/{id}/history` | TRADER | List order history |
| POST | `/orders/{id}/book` | TRADER | Add order to in-memory order book |
| GET | `/order-book` | TRADER | Read BUY and SELL snapshots |
| GET | `/order-book/best-buy` | TRADER | Read highest-price BUY order |
| GET | `/order-book/best-sell` | TRADER | Read lowest-price SELL order |
| GET | `/order-book/recent/{id}` | TRADER | Read from LRU cache |
| POST | `/orders/{id}/queue` | TRADER | Enqueue order for async processing |
| POST | `/orders/{id}/execute-with-broker` | TRADER | Execute via circuit-breaker broker |
| GET | `/external/market-data/{symbol}` | TRADER | Simulated market data with fallback |
| POST | `/execution/start` | ADMIN | Start execution workers |
| POST | `/execution/stop` | ADMIN | Stop execution workers |
| GET | `/execution/stats` | ADMIN | Execution engine statistics |
| GET | `/execution/dlq` | ADMIN | List dead-letter orders |
| POST | `/execution/dlq/{orderId}/reprocess` | ADMIN | Requeue dead-letter order |
| GET | `/events` | ADMIN | List domain events |
| GET | `/events/{type}` | ADMIN | Filter events by type |
| POST | `/orders/{id}/fix-message` | TRADER | Generate simulated FIX message |
| GET | `/orders/{id}/fix-message` | TRADER | Read persisted FIX message |
| GET | `/orders/{id}/fix-explanation` | TRADER | Explain FIX tags |
| POST | `/fix/explain` | TRADER | Parse and explain a raw FIX string |
| GET | `/monitoring/summary` | ADMIN | Operational processing summary |
| GET | `/swagger-ui.html` | Public | Swagger UI |
| GET | `/v3/api-docs` | Public | OpenAPI JSON |
| GET | `/actuator/health` | Public | Spring Boot health |
| GET | `/actuator/prometheus` | Port 8081 | Prometheus metrics |
| GET | `/learning/resilience` | Public | Overview of all resilience patterns |
| GET | `/learning/circuit-breaker` | Public | Circuit breaker states and fallback |
| GET | `/learning/rate-limit` | Public | Rate limiting behaviour |
| GET | `/learning/bulkhead` | Public | Processing isolation and capacity |

## curl examples

```bash
# Create an order
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "clientId": "C001",
    "symbol": "AAPL",
    "side": "BUY",
    "quantity": 100,
    "price": 150.25,
    "idempotencyKey": "REQ-001"
  }'

# Start workers
curl -i -X POST http://localhost:8080/execution/start \
  -H "Authorization: Bearer $TOKEN"

# Queue an order for async processing
curl -i -X POST http://localhost:8080/orders/{id}/queue \
  -H "Authorization: Bearer $TOKEN"

# Read execution stats
curl -s http://localhost:8080/execution/stats \
  -H "Authorization: Bearer $TOKEN" | jq

# Generate FIX message
curl -i -X POST http://localhost:8080/orders/{id}/fix-message \
  -H "Authorization: Bearer $TOKEN"

# Read order book
curl -s http://localhost:8080/order-book \
  -H "Authorization: Bearer $TOKEN" | jq

# Read events
curl -s http://localhost:8080/events \
  -H "Authorization: Bearer $TOKEN" | jq

# Inspect DLQ
curl -s http://localhost:8080/execution/dlq \
  -H "Authorization: Bearer $TOKEN" | jq

# Execute an order through the simulated broker
curl -s -X POST http://localhost:8080/orders/{id}/execute-with-broker \
  -H "Authorization: Bearer $TOKEN" | jq

# Read live simulated market data
curl -s http://localhost:8080/external/market-data/AAPL \
  -H "Authorization: Bearer $TOKEN" | jq

# Trigger the market-data fallback
curl -s http://localhost:8080/external/market-data/MARKET_FAIL \
  -H "Authorization: Bearer $TOKEN" | jq

# Inspect Resilience4j metrics on the management port
curl -s http://localhost:8081/actuator/prometheus \
  | grep resilience4j
```

## Documentation

- `docs/testing-strategy.md`
- `docs/git-workflow.md`
- `docs/phase-03-testing-quality-api-docs.md`
- `docs/phase-04-data-structures-order-book.md`
- `docs/phase-05-concurrency-processing-engine.md`
- `docs/phase-06-fix-message-engine.md`
- `docs/phase-07-event-driven-retry-dlq-idempotency.md`
- `docs/phase-08-observability.md`
- `docs/phase-09-security.md`
- `docs/phase-10-resilience.md`
- `CONTRIBUTING.md`

## Roadmap

Remaining phases: caching/scheduling → Docker Compose →
Kubernetes → performance/load tests → CI/CD.
