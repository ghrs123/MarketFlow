# MarketFlow Senior Java Cloud Lab

> Hands-on study lab for Senior Java/Spring Boot interviews, structured as
> an incremental course. Each phase ships a complete vertical slice with
> its own branch, tests and documentation.

This repository is the implementation of Phase 3 of the
`MarketFlow Senior Java Cloud Lab` curriculum.

## Current phase

**Phase 3 - Testing, Code Quality, OpenAPI and Basic CI**

A quality-focused vertical slice that adds OpenAPI/Swagger, layered test
coverage, JaCoCo reporting, contributor workflow documentation and a
basic GitHub Actions pipeline on top of the persisted Order API.

## Stack

- Java 21
- Spring Boot 3.3.5 (Web, Validation, Actuator, Data JPA)
- Maven
- PostgreSQL + Flyway
- JUnit 5 + Mockito + MockMvc + AssertJ + Testcontainers + JaCoCo

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

Run service unit tests only:

```bash
mvn -Dtest=OrderApplicationServiceTest test
```

Run controller slice tests only:

```bash
mvn -Dtest=OrderControllerTest test
```

Run PostgreSQL integration tests only:

```bash
mvn -Dtest=OrderRepositoryIntegrationTest test
```

Generate JaCoCo report:

```bash
mvn verify
```

Note: Docker is required locally for Testcontainers-backed integration
tests because they start a real PostgreSQL container.

## Endpoints

| Method | Path                              | Purpose                              |
| ------ | --------------------------------- | ------------------------------------ |
| POST   | `/orders`                         | Create a new order                   |
| GET    | `/orders/{id}`                    | Fetch one order by id                |
| GET    | `/orders`                         | Filter + paginate orders             |
| GET    | `/orders/{id}/history`            | List order history                   |
| GET    | `/swagger-ui.html`                | Swagger UI                           |
| GET    | `/v3/api-docs`                    | OpenAPI JSON                         |
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

Swagger UI:

```bash
curl -i http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```bash
curl -s http://localhost:8080/v3/api-docs | jq
```

## Phase 3 docs

- `docs/testing-strategy.md`
- `docs/git-workflow.md`
- `docs/phase-03-testing-quality-api-docs.md`
- `CONTRIBUTING.md`

## Roadmap

Future phases (per the curriculum): data structures + order book ->
concurrency + processing engine -> FIX message engine -> event-driven +
retry + DLQ + idempotency -> external messaging -> observability ->
security -> resilience -> caching/scheduling -> Docker Compose ->
Kubernetes -> performance/load tests -> CI/CD.

See `docs/phase-03-testing-quality-api-docs.md` for the in-depth
narrative of this phase.
