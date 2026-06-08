# Contributing

This repository is structured as an incremental senior Java lab. Each phase is delivered as a complete vertical slice on its own feature branch and must leave the project buildable and testable.

## Branch naming

Use the phase-based branch convention:

```text
feature/NN-slug
```

Examples:

```text
feature/01-foundation-order-api
feature/02-persistence-jpa-transactions
feature/03-testing-quality-api-docs
```

## Conventional Commits

Use the format:

```text
type(scope): description
```

Allowed types:

- `feat`
- `fix`
- `refactor`
- `test`
- `docs`
- `chore`
- `perf`
- `build`
- `ci`

Examples:

```text
feat(order): add OpenAPI documentation for order endpoints
test(order): add standalone MockMvc tests for order controller
ci(build): add GitHub Actions workflow for test and package
docs(phase-03): add testing strategy and workflow guides
```

## How to run locally

Requirements:

- JDK 21
- Maven 3.9+
- PostgreSQL for local application runs
- Docker Desktop or equivalent when running Testcontainers-based tests

Example environment variables:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/marketflow
export DB_USERNAME=marketflow
export DB_PASSWORD=marketflow
```

Start the application:

```bash
mvn spring-boot:run
```

The application listens on port `8080` by default.

## How to run tests

Run the full suite:

```bash
mvn test
```

Package after tests:

```bash
mvn package
```

Notes:

- JPA integration tests use PostgreSQL via Testcontainers.
- Docker must be available locally for Testcontainers-backed tests.
- Do not replace PostgreSQL integration coverage with H2.

## Pull request process

Follow this sequence for every phase or feature:

1. `plan`: define the objective, scope, endpoints, tests, risks, and definition of done.
2. `implement`: make incremental changes that keep the branch working.
3. `review`: validate architecture, error handling, tests, docs, and project rules.
4. `merge`: merge only after tests pass, documentation is updated, and the phase is demonstrable.

Before opening a PR:

- Rebase or update from `main` as needed.
- Run `mvn test`.
- Run `mvn package`.
- Fill in `.github/pull_request_template.md` with concrete evidence.
- Confirm the branch name and commit messages follow project conventions.
