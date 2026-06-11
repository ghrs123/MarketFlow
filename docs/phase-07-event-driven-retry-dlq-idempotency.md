# Phase 7 - Event-driven, Retry, DLQ and Idempotency

## What this phase delivers

Phase 7 turns the existing local execution engine into an event-producing
workflow. Orders now have a durable `idempotencyKey`, processing failures
use capped exponential retry, exhausted orders enter a dead-letter queue,
and operators can inspect events or reprocess a DLQ item through REST.

No external broker is used. Events and dead letters remain process-local so
the phase can focus on delivery semantics before Kafka or RabbitMQ is added.

## Architecture shape

```text
POST /orders
  -> idempotency lookup in PostgreSQL
  -> persist order + history
  -> publish ORDER_VALIDATED and ORDER_CREATED

POST /orders/{id}/queue
  -> bounded BlockingQueue
  -> publish ORDER_QUEUED
  -> named ExecutorService worker
  -> attempt execution
       -> success: ORDER_EXECUTED
       -> failure: ORDER_FAILED
       -> retry: ORDER_RETRIED + exponential backoff
       -> exhausted: persist FAILED state + in-memory DLQ
                     + ORDER_MOVED_TO_DEAD_LETTER

POST /execution/dlq/{orderId}/reprocess
  -> remove DLQ item
  -> reset order to NEW
  -> enqueue with captured MDC
```

## Main components

- `DomainEvent` defines the common immutable event contract.
- Event records represent each relevant business fact.
- `InMemoryEventBus` stores events and delivers them to local subscribers.
- `RetryPolicy` calculates capped exponential delays.
- `RetryRegistry` tracks attempts safely with `ConcurrentHashMap` and `AtomicInteger`.
- `DeadLetterQueue` stores terminal failures by order id.
- `IdempotencyRegistry` resolves keys through the durable order repository.
- `OrderProcessingEngine` orchestrates retry, events, DLQ and reprocessing.

## Idempotency

`POST /orders` requires `idempotencyKey`. Before creation, the application
looks for an existing order with that key. Replays return the existing
aggregate without creating new history or events.

The application lookup improves the common sequential case. The PostgreSQL
unique index is the final protection against concurrent inserts:

```sql
CREATE UNIQUE INDEX uk_orders_idempotency_key
    ON orders (idempotency_key);
```

Existing rows receive their UUID text as a migration-safe key.

## Retry policy

Defaults:

- maximum attempts: `3`
- initial backoff: `100ms`
- maximum backoff: `2000ms`

The delay after a failed attempt is:

```text
min(initialBackoff * 2^(attempt - 1), maximumBackoff)
```

The implementation uses `Thread.sleep` inside the worker deliberately for
clarity. A production system with long delays should use delayed scheduling
or broker-native retry because sleeping occupies a worker thread.

## Dead-letter queue

After the final attempt, the order is marked `FAILED` and a
`DeadLetterMessage` captures:

- order id
- failure reason
- attempt count
- MDC context
- creation timestamp

The queue is in memory. It is thread-safe but not durable, shared between
instances or retained after restart. PostgreSQL or broker-native DLQ storage
would be required in production.

## Endpoints

```text
GET  /events
GET  /events/{type}
GET  /execution/dlq
POST /execution/dlq/{orderId}/reprocess
GET  /learning/event-driven
GET  /learning/idempotency
GET  /learning/dlq
```

## How to run

```bash
export DB_URL=jdbc:postgresql://localhost:5432/marketflow
export DB_USERNAME=marketflow
export DB_PASSWORD=marketflow

mvn spring-boot:run
```

## Demonstration

Create an order that always fails:

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: phase-07-demo" \
  -d '{
    "clientId": "C007",
    "symbol": "FAIL",
    "side": "BUY",
    "quantity": 10,
    "price": 100.00,
    "idempotencyKey": "PHASE-07-FAIL-001"
  }'
```

Repeat the request with the same key and confirm the returned order id does
not change. Then start workers and queue the order:

```bash
curl -i -X POST http://localhost:8080/execution/start
curl -i -X POST http://localhost:8080/orders/{orderId}/queue
curl -s http://localhost:8080/events | jq
curl -s http://localhost:8080/execution/dlq | jq
```

Reprocess it:

```bash
curl -i -X POST http://localhost:8080/execution/dlq/{orderId}/reprocess
```

Because `FAIL` is deterministic in this lab, the reprocessed order will
exhaust retry and return to the DLQ.

## Testing

```bash
mvn test
mvn package
```

The suite covers event storage and subscription, type filtering, concurrent
attempt counting, DLQ replacement/removal, idempotent replay, retry
exhaustion, REST endpoints and PostgreSQL migration validation.

## Technical trade-offs

- Internal events avoid infrastructure overhead, but provide no durability.
- Synchronous subscribers are deterministic, but can delay the publisher.
- In-memory DLQ is simple, but disappears on restart and is not multi-node.
- Database uniqueness closes concurrent idempotency races, while the
  pre-check avoids unnecessary insert failures for normal replays.
- Worker-thread sleep makes backoff visible, but reduces worker capacity.
- Events are published before transaction commit; a transactional outbox is
  the production solution for atomic database and message publication.

## Known risks

- Reusing an idempotency key with a different payload returns the original
  order instead of rejecting the mismatch.
- Event history grows without a retention bound.
- DLQ state is lost on restart.
- Manual reprocessing can repeat a deterministic poison-message failure.
- A process crash between database commit and event publication can lose an
  event because no outbox exists.

## Interview narrative

I extended a bounded, concurrent order engine with event-driven semantics
without introducing an external broker prematurely. Every relevant state
change emits an immutable event that can be queried for demonstration.

For transient failure handling, I implemented a capped exponential retry
policy and a thread-safe attempt registry. After the configured limit, the
order moves to a dead-letter queue and can be explicitly reprocessed.

Order creation is idempotent through a client key stored in PostgreSQL. The
service performs a fast lookup, while a unique database index remains the
authoritative concurrency guard.

I would explicitly state that this is an educational single-process design.
The next production evolution is Kafka or RabbitMQ, persistent DLQ, and a
transactional outbox to connect database commits with event publication.

## Interview questions

1. What is the difference between at-most-once, at-least-once and exactly-once?
2. Why must consumers be idempotent in an at-least-once system?
3. Why is an application lookup insufficient for concurrent idempotency?
4. How does exponential backoff prevent retry storms?
5. Which failures should not be retried?
6. What information should a DLQ message contain?
7. How would a transactional outbox improve this design?
8. What ordering guarantees does an in-memory event list provide?
9. Why is sleeping inside a worker a scalability limitation?
10. How would this design change with multiple application instances?

## Exercises before Phase 8

1. Reject reuse of an idempotency key when the request payload differs.
2. Add retention limits to the event history.
3. Persist DLQ entries in PostgreSQL.
4. Replace worker sleep with a scheduled retry queue.
5. Design an outbox table and publisher lifecycle.

## Definition of Done

- [x] Internal events can be published and queried.
- [x] Order creation requires a durable idempotency key.
- [x] Duplicate keys do not create duplicate orders.
- [x] Failures use capped exponential retry.
- [x] Exhausted orders enter the DLQ.
- [x] DLQ messages can be queried and reprocessed.
- [x] MDC context remains attached to queued and dead-letter work.
- [x] PostgreSQL schema evolves through Flyway.
- [x] Unit, controller and integration tests cover the phase.
- [x] README and phase documentation are updated.

## Suggested commits

```text
feat(event): add internal domain event bus and event catalogue
feat(order): add durable idempotent order creation
feat(execution): add exponential retry registry and policy
feat(execution): add dead-letter queue and reprocessing endpoints
test(phase-07): cover events idempotency retry and DLQ
docs(phase-07): document event-driven retry and idempotency flow
```

## Next step

Phase 8 replaces or bridges the process-local event bus with Kafka or
RabbitMQ and introduces external producer/consumer delivery semantics.
