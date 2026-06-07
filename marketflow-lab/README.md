# MarketFlow Senior Java Cloud Lab

> Hands-on study lab for Senior Java/Spring Boot interviews, structured as
> an incremental course. Each phase ships a complete vertical slice with
> its own branch, tests and documentation.

This repository is the implementation of Phase 1 of the
`MarketFlow Senior Java Cloud Lab` curriculum.

## Current phase

**Phase 1 - Foundation + Order API (in-memory)**

A first vertical slice that exposes the Order management API end-to-end:
controller -> application service -> in-memory repository, plus a
RFC 7807 global exception handler, Bean Validation and Actuator. JPA and
PostgreSQL are intentionally deferred to Phase 2.

## Stack

- Java 21
- Spring Boot 3.3.5 (Web, Validation, Actuator)
- Maven
- JUnit 5 + MockMvc + AssertJ

## Project layout

```
src/main/java/com/gustavo/marketflow
├── MarketFlowApplication.java
├── order
│   ├── api              # REST adapter (controller + DTOs)
│   ├── application      # use cases (OrderApplicationService)
│   ├── domain           # Order, enums, repository port
│   └── infrastructure   # OrderInMemoryRepository
├── shared
│   └── exception        # GlobalExceptionHandler + domain exceptions
├── monitoring
│   └── api              # CustomHealthController
└── learning             # /learning/* didactic endpoints
```

## How to run

Requires JDK 21 and Maven 3.9+.

```bash
mvn spring-boot:run
```

Default port: `8080`.

## How to test

```bash
mvn test
```

## Endpoints

| Method | Path                              | Purpose                              |
| ------ | --------------------------------- | ------------------------------------ |
| POST   | `/orders`                         | Create a new order                   |
| GET    | `/orders/{id}`                    | Fetch one order by id                |
| GET    | `/orders`                         | List all orders                      |
| GET    | `/health/custom`                  | Application-owned health summary     |
| GET    | `/actuator/health`                | Spring Boot Actuator health          |
| GET    | `/actuator/info`                  | Application info                     |
| GET    | `/actuator/metrics`               | Available metrics                    |
| GET    | `/learning/spring/beans`          | Notes on Spring DI + bean inspection |
| GET    | `/learning/rest`                  | Notes on REST design                 |
| GET    | `/learning/validation`            | Notes on Bean Validation             |
| GET    | `/learning/exception-handling`    | Notes on the error model             |

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

Trigger a validation error (expect 400 with RFC 7807 body):

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

Trigger a not-found error (expect 404 with RFC 7807 body):

```bash
curl -i http://localhost:8080/orders/00000000-0000-0000-0000-000000000000
```

List orders:

```bash
curl -s http://localhost:8080/orders | jq
```

## Roadmap

Future phases (per the curriculum): PostgreSQL + JPA + transactions ->
testing/quality + OpenAPI -> data structures + order book -> concurrency
+ processing engine -> FIX message engine -> event-driven + retry + DLQ
+ idempotency -> external messaging -> observability -> security ->
resilience -> caching/scheduling -> Docker Compose -> Kubernetes ->
performance/load tests -> CI/CD.

See `docs/phase-01-foundation-order-api.md` for the in-depth narrative of
this phase.
