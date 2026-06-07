# MarketFlow Senior Java Cloud Lab

> Hands-on study lab for Senior Java/Spring Boot interviews, structured as
> an incremental course. Each phase ships a complete vertical slice with
> its own branch, tests and documentation.

This repository is the implementation of Phase 2 of the
`MarketFlow Senior Java Cloud Lab` curriculum.

## Current phase

**Phase 2 - PostgreSQL + JPA + Transactions + Order History**

A persistence-focused vertical slice that upgrades the Order API from
in-memory storage to PostgreSQL with Spring Data JPA, Flyway migrations,
transactional consistency and order-history tracking.

## Stack

- Java 21
- Spring Boot 3.3.5 (Web, Validation, Actuator, Data JPA)
- Maven
- PostgreSQL + Flyway
- JUnit 5 + MockMvc + AssertJ + Testcontainers

## Project layout

```
src/main/java/com/gustavo/marketflow
├── MarketFlowApplication.java
├── order
│   ├── api              # REST adapter (controller + DTOs)
│   ├── application      # use cases (OrderApplicationService)
│   ├── domain           # Order, enums, repository port
│   └── infrastructure   # JPA entities/repositories/adapters
├── shared
│   └── exception        # GlobalExceptionHandler + domain exceptions
├── monitoring
│   └── api              # CustomHealthController
└── learning             # /learning/* didactic endpoints
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

```bash
mvn test
```

## Endpoints

| Method | Path                              | Purpose                              |
| ------ | --------------------------------- | ------------------------------------ |
| POST   | `/orders`                         | Create a new order                   |
| GET    | `/orders/{id}`                    | Fetch one order by id                |
| GET    | `/orders`                         | Filter + paginate orders             |
| GET    | `/orders/{id}/history`            | List order history                   |
| GET    | `/health/custom`                  | Application-owned health summary     |
| GET    | `/actuator/health`                | Spring Boot Actuator health          |
| GET    | `/actuator/info`                  | Application info                     |
| GET    | `/actuator/metrics`               | Available metrics                    |
| GET    | `/learning/spring/beans`          | Notes on Spring DI + bean inspection |
| GET    | `/learning/rest`                  | Notes on REST design                 |
| GET    | `/learning/validation`            | Notes on Bean Validation             |
| GET    | `/learning/exception-handling`    | Notes on the error model             |
| GET    | `/learning/jpa/lazy-vs-eager`     | Notes on LAZY vs EAGER loading       |
| GET    | `/learning/jpa/n-plus-one`        | Notes on N+1 and mitigation          |
| GET    | `/learning/transactions`          | Notes on transactional boundaries    |
| GET    | `/learning/transactions/self-invocation` | Notes on proxy/self invocation |
| GET    | `/learning/hibernate/dirty-checking` | Notes on dirty checking          |

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

List orders with filters:

```bash
curl -s "http://localhost:8080/orders?clientId=C001&status=NEW&page=0&size=20" | jq
```

Order history:

```bash
curl -s http://localhost:8080/orders/{id}/history | jq
```

## Roadmap

Future phases (per the curriculum): PostgreSQL + JPA + transactions ->
testing/quality + OpenAPI -> data structures + order book -> concurrency
+ processing engine -> FIX message engine -> event-driven + retry + DLQ
+ idempotency -> external messaging -> observability -> security ->
resilience -> caching/scheduling -> Docker Compose -> Kubernetes ->
performance/load tests -> CI/CD.

See `docs/phase-02-persistence-jpa-transactions.md` for the in-depth
narrative of this phase.
