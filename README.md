# MarketFlow Senior Java Cloud Lab

> Hands-on study lab for Senior Java/Spring Boot interviews, structured as
> an incremental course. Each phase ships a complete vertical slice with
> its own branch, tests and documentation.

This repository is the implementation of Phase 4 of the
`MarketFlow Senior Java Cloud Lab` curriculum.

## Current phase

**Phase 4 - Data Structures and In-Memory Order Book**

An in-memory data-structure slice that adds a price-ordered order book,
a recent-order LRU cache and learning endpoints on top of the persisted
Order API delivered in the earlier phases.

## Stack

- Java 21
- Spring Boot 3.3.5 (Web, Validation, Actuator, Data JPA)
- Maven
- PostgreSQL + Flyway
- JUnit 5 + Mockito + MockMvc + AssertJ + Testcontainers + JaCoCo
- In-memory data structures: `PriorityQueue`, `LinkedHashMap`, `ConcurrentHashMap`

## Project layout

```text
src/main/java/com/gustavo/marketflow
|- MarketFlowApplication.java
|- order
|  |- api              # REST adapter (controller + DTOs)
|  |- application      # use cases (OrderApplicationService)
|  |- domain           # Order, enums, repository port
|  `- infrastructure   # JPA entities/repositories/adapters
|- orderbook
|  |- api              # OrderBookController + response DTOs
|  |- application      # OrderBookApplicationService
|  `- domain           # OrderBook, OrderTask, RecentOrderCache
|- shared
|  `- exception        # GlobalExceptionHandler + domain exceptions
|- monitoring
|  `- api              # CustomHealthController
`- learning            # /learning/* didactic endpoints
```

## How to run

Requires JDK 21, Maven 3.9+ and a PostgreSQL instance.

Example local environment variables:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/marketflow
export DB_USERNAME=marketflow
export DB_PASSWORD=marketflow
```

```bash
mvn spring-boot:run
```

Default port: `8080`.

## How to test

Run the full suite:

```bash
mvn test
```

Run order service unit tests only:

```bash
mvn -Dtest=OrderApplicationServiceTest test
```

Run HTTP controller slice tests only:

```bash
mvn -Dtest=OrderControllerTest,OrderBookControllerTest test
```

Run order book unit tests only:

```bash
mvn -Dtest=OrderBookTest,RecentOrderCacheTest,OrderBookApplicationServiceTest test
```

Run PostgreSQL integration tests only:

```bash
mvn -Dtest=OrderRepositoryIntegrationTest test
```

Generate the JaCoCo report:

```bash
mvn verify
```

Note: Docker is required locally for Testcontainers-backed integration
tests because they start a real PostgreSQL container.

## Endpoints

| Method | Path | Purpose |
| ------ | ---- | ------- |
| POST | `/orders` | Create a new persisted order |
| GET | `/orders/{id}` | Fetch one order by id |
| GET | `/orders` | Filter and paginate orders |
| GET | `/orders/{id}/history` | List order history |
| POST | `/orders/{id}/book` | Add a persisted order to the in-memory order book |
| GET | `/order-book` | Read BUY and SELL snapshots |
| GET | `/order-book/best-buy` | Read highest-price BUY order |
| GET | `/order-book/best-sell` | Read lowest-price SELL order |
| GET | `/order-book/recent/{id}` | Read a recent order from the LRU cache |
| GET | `/swagger-ui.html` | Swagger UI |
| GET | `/v3/api-docs` | OpenAPI JSON |
| GET | `/health/custom` | Application-owned health summary |
| GET | `/actuator/health` | Spring Boot Actuator health |
| GET | `/actuator/info` | Application info |
| GET | `/actuator/metrics` | Available metrics |
| GET | `/learning/spring/beans` | Notes on Spring DI and bean inspection |
| GET | `/learning/rest` | Notes on REST design |
| GET | `/learning/validation` | Notes on Bean Validation |
| GET | `/learning/exception-handling` | Notes on the error model |
| GET | `/learning/jpa/lazy-vs-eager` | Notes on LAZY vs EAGER loading |
| GET | `/learning/jpa/n-plus-one` | Notes on N+1 and mitigation |
| GET | `/learning/transactions` | Notes on transactional boundaries |
| GET | `/learning/transactions/self-invocation` | Notes on proxy/self invocation |
| GET | `/learning/hibernate/dirty-checking` | Notes on dirty checking |
| GET | `/learning/data-structures` | Overview of the Phase 4 structure choices |
| GET | `/learning/data-structures/order-book` | Notes on `PriorityQueue` in the order book |
| GET | `/learning/data-structures/cache` | Notes on the recent-order cache |
| GET | `/learning/data-structures/concurrent-map` | Notes on `ConcurrentHashMap` lookup |

## curl examples

Create an order:

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "C001",
    "symbol": "AAPL",
    "side": "BUY",
    "quantity": 100,
    "price": 150.25
  }'
```

Trigger a validation error:

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "",
    "symbol": "AAPL",
    "side": "BUY",
    "quantity": -1,
    "price": 0
  }'
```

Trigger a not-found error:

```bash
curl -i http://localhost:8080/orders/00000000-0000-0000-0000-000000000000
```

List persisted orders with filters:

```bash
curl -s "http://localhost:8080/orders?clientId=C001&status=NEW&page=0&size=20" | jq
```

List order history:

```bash
curl -s http://localhost:8080/orders/{id}/history | jq
```

Add a persisted order to the in-memory order book:

```bash
curl -i -X POST http://localhost:8080/orders/{id}/book
```

Read the current order book snapshot:

```bash
curl -s http://localhost:8080/order-book | jq
```

Read the best BUY and best SELL:

```bash
curl -s http://localhost:8080/order-book/best-buy | jq
curl -s http://localhost:8080/order-book/best-sell | jq
```

Read a recent cached order:

```bash
curl -s http://localhost:8080/order-book/recent/{id} | jq
```

Swagger UI:

```bash
curl -i http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```bash
curl -s http://localhost:8080/v3/api-docs | jq
```

## Documentation

- `docs/testing-strategy.md`
- `docs/git-workflow.md`
- `docs/phase-03-testing-quality-api-docs.md`
- `docs/phase-04-data-structures-order-book.md`
- `CONTRIBUTING.md`

## Roadmap

Future phases (per the curriculum): concurrency + processing engine ->
FIX message engine -> event-driven + retry + DLQ + idempotency ->
external messaging -> observability -> security -> resilience ->
caching/scheduling -> Docker Compose -> Kubernetes ->
performance/load tests -> CI/CD.

See `docs/phase-04-data-structures-order-book.md` for the in-depth
narrative of this phase.
