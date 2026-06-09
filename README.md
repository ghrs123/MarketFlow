# MarketFlow Senior Java Cloud Lab

> Hands-on study lab for Senior Java/Spring Boot interviews, structured as
> an incremental course. Each phase ships a complete vertical slice with
> its own branch, tests and documentation.

This repository is the implementation of Phase 5 of the
`MarketFlow Senior Java Cloud Lab` curriculum.

## Current phase

**Phase 5 - Concurrency and Processing Engine**

This phase adds an in-process execution engine with a bounded
`BlockingQueue`, named worker threads managed by `ExecutorService`,
thread-safe execution statistics, MDC propagation to async workers and
learning endpoints focused on concurrency concepts.

## Stack

- Java 21
- Spring Boot 3.3.5 (Web, Validation, Actuator, Data JPA)
- Maven
- PostgreSQL + Flyway
- JUnit 5 + Mockito + MockMvc + AssertJ + Testcontainers + JaCoCo
- In-memory data structures: `PriorityQueue`, `LinkedHashMap`, `ConcurrentHashMap`, `BlockingQueue`
- Concurrency primitives: `ExecutorService`, `CompletableFuture`, `AtomicLong`, `ReentrantLock`

## Project layout

```text
src/main/java/com/gustavo/marketflow
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
|- shared
|  |- config
|  |- exception
|  `- logging
`- monitoring
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

Important runtime defaults:

- execution worker count: `4`
- internal queue capacity: `100`
- simulated processing delay: `50ms`

Every inbound HTTP request receives an `X-Correlation-Id` header. If the
client does not provide one, the service generates it and propagates it to
async worker threads through MDC context capture.

## How to test

Run the full suite:

```bash
mvn test
```

Run the existing order and order-book tests only:

```bash
mvn -Dtest=OrderApplicationServiceTest,OrderControllerTest,OrderRepositoryIntegrationTest,OrderBookApplicationServiceTest,OrderBookControllerTest,OrderBookTest,RecentOrderCacheTest test
```

Run Phase 5 tests only:

```bash
mvn -Dtest=OrderProcessingEngineTest,ExecutionControllerTest,ExecutionStatisticsTest,RaceConditionDemoTest,AtomicCounterDemoTest,CompletableFutureDemoTest test
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
| POST | `/orders/{id}/queue` | Enqueue an order for asynchronous processing |
| POST | `/execution/start` | Start execution workers |
| POST | `/execution/stop` | Stop execution workers |
| GET | `/execution/stats` | Read execution-engine statistics |
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
| GET | `/learning/concurrency/race-condition` | Unsafe vs safe shared-counter updates |
| GET | `/learning/concurrency/deadlock` | Deadlock and starvation explanation |
| GET | `/learning/concurrency/completable-future` | `CompletableFuture` with named executor and MDC |
| GET | `/learning/concurrency/thread-pool` | Worker-pool sizing and queue discussion |

## curl examples

Create an order that should execute successfully:

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: demo-success-1" \
  -d '{
    "clientId": "C001",
    "symbol": "AAPL",
    "side": "BUY",
    "quantity": 100,
    "price": 150.25
  }'
```

Create an order that should fail during processing:

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: demo-fail-1" \
  -d '{
    "clientId": "C002",
    "symbol": "FAIL",
    "side": "SELL",
    "quantity": 50,
    "price": 90.10
  }'
```

Start workers:

```bash
curl -i -X POST http://localhost:8080/execution/start
```

Queue an order for async processing:

```bash
curl -i -X POST http://localhost:8080/orders/{id}/queue
```

Read execution stats:

```bash
curl -s http://localhost:8080/execution/stats | jq
```

Stop workers:

```bash
curl -i -X POST http://localhost:8080/execution/stop
```

Read the order book snapshot:

```bash
curl -s http://localhost:8080/order-book | jq
```

Read concurrency learning endpoints:

```bash
curl -s http://localhost:8080/learning/concurrency/race-condition | jq
curl -s http://localhost:8080/learning/concurrency/completable-future | jq
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
- `docs/phase-05-concurrency-processing-engine.md`
- `CONTRIBUTING.md`

## Roadmap

Future phases (per the curriculum): FIX message engine ->
event-driven + retry + DLQ + idempotency -> external messaging ->
observability -> security -> resilience -> caching/scheduling ->
Docker Compose -> Kubernetes -> performance/load tests -> CI/CD.

See `docs/phase-05-concurrency-processing-engine.md` for the in-depth
narrative of this phase.
