# Testing Strategy

## Adopted test pyramid

The project uses a pragmatic three-layer test pyramid:

1. `unit`: fast tests with no Spring context and no database.
2. `slice`: focused HTTP tests with standalone `MockMvc`.
3. `integration`: Spring Boot + PostgreSQL via Testcontainers.

The goal is behavioural confidence, not raw coverage percentage.

## What each layer tests

### Unit

Target:

- application services
- domain behaviour
- orchestration logic

What it tests:

- business flow
- failure paths
- repository and collaborator interaction
- page calculation and exception propagation

What it does not test:

- Spring wiring
- HTTP serialization
- Bean Validation integration
- real database behaviour

### Slice

Target:

- controllers
- request binding
- validation
- RFC 7807 error mapping

What it tests:

- status codes
- headers such as `Location`
- JSON request and response contracts
- validation failures
- malformed JSON
- enum conversion
- path/query parameter binding

What it does not test:

- real persistence
- transaction boundaries
- Spring Boot application startup

### Integration

Target:

- JPA adapters
- Spring Data repositories
- Flyway-managed schema
- optimistic locking
- Spring AOP proxies and Resilience4j annotations

What it tests:

- PostgreSQL persistence and retrieval
- filtering and pagination
- history ordering
- optimistic locking conflicts
- mapping from domain to JPA and back
- circuit breaker fallback through the real Spring proxy
- retry recovery and exhaustion through the real Spring proxy

What it does not test:

- controller HTTP contract
- service orchestration in isolation

## Tooling

- `JUnit 5` for test execution
- `Mockito` for collaborator mocking in unit and controller tests
- `AssertJ` for all assertions
- `MockMvc` standalone setup for controller slice tests
- `Testcontainers` with PostgreSQL for integration tests

## Project rules

- Do not use H2 for JPA integration tests.
- Use `isEqualByComparingTo()` for `BigDecimal` assertions.
- Use `OrderTestData` builders for reusable valid fixtures.
- Keep tests independent; no shared mutable state between methods.
- Clean persisted data in `@BeforeEach` for integration suites.

## How to run locally

Run all tests:

```bash
mvn test
```

Run unit service tests only:

```bash
mvn -Dtest=OrderApplicationServiceTest test
```

Run controller slice tests only:

```bash
mvn -Dtest=OrderControllerTest test
```

Run JPA integration tests only:

```bash
mvn -Dtest=OrderRepositoryIntegrationTest test
```

Run Phase 10 resilience tests:

```bash
mvn -Dtest="ResiliencePatternsTest,ResilienceApplicationIntegrationTest,ExternalServiceControllerTest,ExecutionControllerTest,OrderProcessingEngineTest" test
```

Generate JaCoCo report:

```bash
mvn verify
```

Notes:

- Docker must be available locally for Testcontainers-backed tests.
- Integration tests start a real PostgreSQL container automatically.
- The latest full suite contains 157 tests with zero failures and zero errors.
