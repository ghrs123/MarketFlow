# Phase 3 - Testing, Code Quality, OpenAPI and Basic CI

## What this phase delivers

Phase 3 upgrades the lab from a working persistence slice into a more
professional delivery baseline. The project now exposes OpenAPI
documentation, separates tests by layer, runs PostgreSQL integration
tests through Testcontainers and validates the branch in GitHub Actions.

This phase is complete when Swagger is accessible, tests are clearly
structured by responsibility, CI runs build and tests automatically, and
contributors have a documented workflow for branches and pull requests.

## Technical decisions and trade-offs

### JaCoCo without a quality gate

JaCoCo was added to generate coverage reports, but the build does not
fail on a percentage threshold. The trade-off is deliberate: behavioural
tests remain the primary goal, and the team avoids writing low-value
tests only to satisfy a numeric target.

### `springdoc-openapi` 2.6.0

Swagger and the OpenAPI document are generated from the actual Spring MVC
controllers and DTOs. This keeps the API documentation close to the
executable contract and avoids maintaining a second, hand-written spec.

### Standalone `MockMvc`

Controller tests use standalone `MockMvc` instead of `@SpringBootTest`.
This keeps the slice tests fast and focused on request binding,
validation, headers and RFC 7807 responses, while leaving real
persistence concerns to the integration layer.

## Test structure by layer

### Unit

Example:

- `OrderApplicationServiceTest`

Focus:

- service orchestration
- repository collaboration
- rollback path signalling
- not-found behaviour
- filter and pagination result shaping

### HTTP slice

Example:

- `OrderControllerTest`

Focus:

- `POST /orders` status and `Location`
- validation errors
- malformed JSON
- invalid enum values
- invalid UUID path input
- RFC 7807 response bodies

### Integration

Example:

- `OrderRepositoryIntegrationTest`

Focus:

- `save` and `findById`
- filter combinations
- pagination
- count queries
- order history ordering
- optimistic locking conflict

## How to run

Application:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/marketflow
export DB_USERNAME=marketflow
export DB_PASSWORD=marketflow

mvn spring-boot:run
```

Tests:

```bash
mvn test
mvn package
```

Swagger UI:

```bash
curl -i http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```bash
curl -s http://localhost:8080/v3/api-docs | jq
```

## Interview narrative

In this phase I turned a functional Spring Boot service into something
closer to a professional engineering baseline. The focus was not new
business capability, but the quality envelope around the existing order
API: documentation, test layering and automated validation in CI.

I split the tests by responsibility. Service logic is verified with pure
Mockito unit tests, controller behaviour is covered with standalone
`MockMvc`, and persistence is validated only against real PostgreSQL via
Testcontainers. That separation keeps feedback fast without sacrificing
confidence in the database-specific behaviour.

I also added OpenAPI and a minimal GitHub Actions pipeline. Swagger makes
the HTTP contract explorable, and CI makes the branch self-checking with
`mvn test` and `mvn package`. A key trade-off here is that JaCoCo is
informative only; I wanted visibility into coverage without incentivising
meaningless tests written just to satisfy a gate.

## Interview questions and answer skeletons

1. Why use standalone `MockMvc` instead of `@SpringBootTest` for controller tests?
   Answer skeleton: isolate HTTP contract concerns, reduce startup time, keep persistence out of controller tests, still use real validation and exception handling.

2. Why keep Testcontainers for repository tests instead of H2?
   Answer skeleton: PostgreSQL-specific behaviour matters, schema and SQL are validated realistically, avoids false confidence from H2 compatibility gaps.

3. Why add JaCoCo but no coverage gate?
   Answer skeleton: coverage visibility is useful, percentage alone is a weak quality proxy, behaviour-first testing is the goal.

4. What does OpenAPI improve in a backend service?
   Answer skeleton: discoverability, faster onboarding, easier manual verification, contract visibility for consumers.

5. What belongs in a unit test versus an integration test?
   Answer skeleton: unit tests isolate logic and collaborators, integration tests validate real framework/database wiring and persistence semantics.

6. Why is `OrderTestData` useful?
   Answer skeleton: reusable valid fixtures, less duplication, more readable tests, centralized business-safe defaults.

7. How do you test RFC 7807 responses properly?
   Answer skeleton: assert status, `type`, `title`, detail/extensions, and drive real error conditions such as validation, malformed input and not found.

8. Why run both `mvn test` and `mvn package` in CI?
   Answer skeleton: tests verify behaviour, package verifies the project can still be assembled into a distributable artifact.

9. Why keep controller tests out of the Spring context?
   Answer skeleton: speed, determinism, smaller failure surface, clearer ownership of what the test is proving.

10. What production risk does optimistic locking protect against?
    Answer skeleton: lost updates during concurrent modification, conflict detection at write time, explicit retry or refresh strategy.

## Definition of Done

- [x] Swagger UI is accessible
- [x] OpenAPI JSON is exposed
- [x] Unit tests exist for service behaviour
- [x] Standalone MockMvc tests cover controller contract and RFC 7807 errors
- [x] PostgreSQL integration tests run through Testcontainers
- [x] JaCoCo report generation is configured
- [x] GitHub Actions runs test and package
- [x] PR template exists
- [x] CONTRIBUTING guide exists
- [x] Testing and Git workflow documentation exists

## Suggested commits

```text
test(order): add service unit tests with Mockito
test(order): add standalone MockMvc controller tests
test(order): add PostgreSQL repository integration coverage
build(quality): add JaCoCo report generation
feat(openapi): add Swagger UI and OpenAPI metadata
ci(build): add GitHub Actions test and package workflow
docs: add pull request template and contributing guide
docs(phase-03): add testing strategy, git workflow and phase documentation
```
