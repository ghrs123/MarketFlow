# Phase 2 - PostgreSQL + JPA + Transactions + History

## What this phase delivers

Phase 2 replaces in-memory persistence with PostgreSQL via Spring Data JPA,
adds Flyway migrations, introduces transactional consistency for order creation
and stores lifecycle history for each order.

This phase is complete when order data survives application restarts, filters
and pagination work, history is queryable and transactional rollback is proven
by tests.

## What was implemented

1. PostgreSQL configuration in `application.yml` with env-based credentials.
2. Flyway migration creating `orders` and `order_history` tables.
3. `OrderEntity` with `@Version` optimistic locking.
4. `OrderHistoryEntity` for auditable order events.
5. Spring Data JPA repositories and JPA adapters for domain ports.
6. Transactional order creation in `OrderApplicationService`.
7. History event persistence (`ORDER_CREATED`) in the same transaction.
8. Filtered/paginated listing endpoint.
9. History endpoint by order id.
10. Learning endpoints for JPA/transactions topics.
11. Integration tests with Testcontainers PostgreSQL.

## Endpoint contract

- `POST /orders`
- `GET /orders/{id}`
- `GET /orders?clientId=&status=&page=&size=`
- `GET /orders/{id}/history`
- `GET /learning/jpa/lazy-vs-eager`
- `GET /learning/jpa/n-plus-one`
- `GET /learning/transactions`
- `GET /learning/transactions/self-invocation`
- `GET /learning/hibernate/dirty-checking`

## Transaction boundary

`OrderApplicationService.createOrder(...)` is annotated with `@Transactional`.
Order and history are written atomically. If history persistence fails, order
creation is rolled back.

## Why this design

1. Keep controller thin and preserve service orchestration role.
2. Preserve domain port pattern while swapping infrastructure adapter.
3. Use SQL migrations for deterministic schema evolution.
4. Use optimistic locking (`@Version`) for safe concurrent updates.

## Test evidence covered

1. Context boot with PostgreSQL container.
2. Repository persistence and filtered pagination.
3. Service rollback when history write fails.
4. Controller integration for create/filter/history flows.
5. Optimistic locking conflict under concurrent updates.

## Local run

Set environment variables before startup:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/marketflow
export DB_USERNAME=marketflow
export DB_PASSWORD=marketflow
```

Run:

```bash
mvn spring-boot:run
```

## Suggested commits

```text
feat: replace in-memory repository with JPA persistence
feat: add order history entity and repository
feat: add database migrations
feat: add transactional order creation
feat: add order filters and pagination
test: add PostgreSQL integration tests with Testcontainers
docs: add phase 02 persistence documentation
```
