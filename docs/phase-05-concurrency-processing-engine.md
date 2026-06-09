# Phase 5 - Concurrency and Processing Engine

## What this phase delivers

Phase 5 adds the first real asynchronous processing flow to the project.
Orders can now be queued into a bounded internal `BlockingQueue`, consumed
by named worker threads and transitioned through execution states without
introducing Kafka, RabbitMQ or any external broker yet.

This phase also adds thread-safe execution statistics, request
correlation-id propagation from HTTP threads to worker threads, and
learning endpoints that demonstrate race conditions, synchronization,
locks, deadlocks, starvation and `CompletableFuture`.

## Architecture shape

The implementation keeps the modular-monolith shape:

- `execution.domain`
  - `OrderQueue`
  - `QueuedOrder`
- `execution.application`
  - `OrderProcessingEngine`
  - `ExecutionWorker`
  - `OrderExecutionService`
  - `ExecutionStatistics`
  - `ExecutionResult`
  - `ExecutionStats`
- `execution.api`
  - `ExecutionController`
  - `ExecutionStatsResponse`
- `learning.concurrency`
  - race-condition, lock and `CompletableFuture` demos
- `shared.logging`
  - `CorrelationIdFilter`

The engine owns queue lifecycle and worker orchestration. Transactional
order state transitions stay in `OrderExecutionService`, so persistence and
history remain atomic per transition.

## Key technical decisions and trade-offs

### Why `BlockingQueue` now

The goal of this phase is to learn the producer-consumer model without
adding infrastructure complexity too early. `BlockingQueue` makes the
backpressure and worker hand-off explicit while keeping the runtime local
and debuggable.

Trade-off:

- simpler to reason about than Kafka or RabbitMQ
- no durability, partitioning or cross-process scaling yet
- a process crash loses queued work

### Why a named `ExecutorService`

Workers run on an application-managed fixed thread pool, not
`ForkJoinPool.commonPool()`. That makes worker count explicit, thread names
observable in logs and shutdown controlled by the service.

Trade-off:

- bounded and predictable
- can saturate under load
- requires conscious sizing for CPU-bound vs IO-bound work

### Why split `OrderProcessingEngine` and `OrderExecutionService`

The engine is not transactional because it manages queue state and thread
lifecycle. Actual state transitions are delegated to a dedicated service so
`@Transactional` remains on the service layer only.

Trade-off:

- clearer layering and safer persistence semantics
- more classes than a monolithic worker implementation
- easier to test and reason about

### Why `MDC.getCopyOfContextMap()` on enqueue

The HTTP thread and worker thread are different execution contexts. MDC
does not propagate automatically, so the queue payload captures the context
map and the worker restores it before processing.

Trade-off:

- keeps correlation id continuity in async logs
- requires explicit code at the async boundary
- context must be small and non-sensitive

## Request lifecycle

### `POST /orders/{id}/queue`

1. Client sends the queue request, optionally with `X-Correlation-Id`.
2. `CorrelationIdFilter` ensures the request has a correlation id and puts
   it into MDC.
3. `ExecutionController` validates the UUID and delegates.
4. `OrderProcessingEngine` verifies that the order exists and is in
   `NEW` status.
5. The engine captures MDC into `QueuedOrder`.
6. `OrderQueue` inserts the payload into the bounded `BlockingQueue`.
7. The engine records `ORDER_QUEUED` in order history and increments queue
   metrics and counters.
8. The API returns `202 Accepted`.

### `POST /execution/start`

1. Controller delegates to `OrderProcessingEngine.start()`.
2. The engine flips the running flag and submits `ExecutionWorker`
   instances to the named executor.
3. Each worker polls the queue with timeout and waits for work.

### Worker processing

1. Worker polls a `QueuedOrder`.
2. Worker restores captured MDC.
3. `OrderExecutionService.markAccepted(...)` transitions the order to
   `ACCEPTED` and records history.
4. The engine simulates success or failure.
5. Success path:
   - `markExecuted(...)`
   - history event `ORDER_EXECUTED`
   - success counter and timer update
6. Failure path:
   - `markFailed(...)`
   - history event `ORDER_FAILED`
   - failure counter and timer update
7. The order id is released from the queue index so it can be enqueued
   again only if business rules allow it in a later phase.

### `GET /execution/stats`

1. Controller asks the engine for a snapshot.
2. The engine reads atomics and queue size.
3. A DTO is returned with worker and processing counters.

## Failure paths

- invalid UUID -> `400` via global exception handler
- missing order -> `404`
- order already queued -> `409`
- order not in `NEW` status -> `409`
- queue full -> `503`
- unexpected worker exception -> order is marked `FAILED` when possible,
  counters move to failure and the worker keeps running

## Concurrency concepts demonstrated

- `BlockingQueue` for producer-consumer hand-off
- `ExecutorService` for bounded worker management
- `AtomicLong` for lock-free counters
- `synchronized` vs `AtomicInteger` vs `ReentrantLock`
- race condition amplification and correction
- deadlock and starvation explanation
- `CompletableFuture` with explicit executor and MDC propagation

## How to run

```bash
export DB_URL=jdbc:postgresql://localhost:5432/marketflow
export DB_USERNAME=marketflow
export DB_PASSWORD=marketflow

mvn spring-boot:run
```

## How to test

Run the full suite:

```bash
mvn test
```

Run only Phase 5 tests:

```bash
mvn -Dtest=OrderProcessingEngineTest,ExecutionControllerTest,ExecutionStatisticsTest,RaceConditionDemoTest,AtomicCounterDemoTest,CompletableFutureDemoTest test
```

## curl examples

Create order:

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: phase5-demo-1" \
  -d '{
    "clientId": "C001",
    "symbol": "AAPL",
    "side": "BUY",
    "quantity": 100,
    "price": 150.25
  }'
```

Start workers:

```bash
curl -i -X POST http://localhost:8080/execution/start
```

Queue order:

```bash
curl -i -X POST http://localhost:8080/orders/{id}/queue
```

Read stats:

```bash
curl -s http://localhost:8080/execution/stats | jq
```

Stop workers:

```bash
curl -i -X POST http://localhost:8080/execution/stop
```

## Interview narrative

In Phase 5 I introduced the first asynchronous processing step into the
project. Instead of jumping straight to Kafka, I deliberately used a
bounded `BlockingQueue` plus a named `ExecutorService` so the
producer-consumer model stays explicit and easy to debug.

The main architectural decision was to separate queue orchestration from
transactional persistence. `OrderProcessingEngine` owns worker lifecycle,
queue state, counters and MDC restoration, while `OrderExecutionService`
owns the actual order state transitions and history writes under
`@Transactional`.

I also treated observability as part of the feature, not an afterthought.
Every HTTP request gets a correlation id, that context is copied into the
queued payload, and worker threads restore it before processing. That means
async logs can still be traced back to the original request, which is the
kind of detail that matters in real backend systems.

## Interview questions and answer skeletons

1. Why use `BlockingQueue` before introducing Kafka?
   Answer skeleton: phase scope is concurrency fundamentals, local queue keeps the producer-consumer model explicit, avoids premature infrastructure complexity.

2. Why avoid `ForkJoinPool.commonPool()` in backend services?
   Answer skeleton: shared global pool, poor isolation, harder observability, thread usage becomes implicit, application-managed pool gives control.

3. Why is `OrderExecutionService` separate from `OrderProcessingEngine`?
   Answer skeleton: keep transactions on service layer, engine is thread/lifecycle orchestration, persistence transitions remain atomic and testable.

4. How do you prevent duplicate queue entries?
   Answer skeleton: queue wrapper keeps a concurrent id index, enqueue rejects duplicates before queue insertion.

5. Why not process directly on the HTTP thread?
   Answer skeleton: decouple request latency from processing latency, model async backpressure, prepare for later broker-based evolution.

6. Why use atomics for stats?
   Answer skeleton: counters are shared across workers, lock-free increments are enough, no larger invariant needs coarse synchronization there.

7. How is MDC propagated to worker threads?
   Answer skeleton: capture context map at enqueue time, restore in worker before processing, clear or restore previous context afterwards.

8. What status transitions happen during processing?
   Answer skeleton: `NEW` -> `ACCEPTED` -> `EXECUTED` or `FAILED`, each persisted with order history.

9. What are the limitations of this phase compared with a real broker?
   Answer skeleton: no durability, no cross-instance scaling, no replay, no partitioning, process crash loses queued work.

10. What would you change first if throughput became a problem?
    Answer skeleton: measure queue growth and worker saturation, revisit pool sizing, separate CPU vs IO work, move to external messaging in later phases.

## Exercises before the next phase

1. Add a second outcome policy based on symbol prefix instead of only exact `FAIL`.
2. Add a lightweight integration test that waits for an order to reach `EXECUTED`.
3. Expose the last N execution results in `/execution/stats`.
4. Add separate gauges for active workers and running flag.
5. Compare `LinkedBlockingQueue` with `ArrayBlockingQueue` and document the trade-offs.

## Definition of Done

- [x] Orders can be enqueued through `POST /orders/{id}/queue`
- [x] Workers can be started and stopped through REST endpoints
- [x] Orders are processed asynchronously on named worker threads
- [x] Execution stats are thread-safe and exposed through `/execution/stats`
- [x] Success and failure can be simulated
- [x] Race condition and safe-counter demos exist
- [x] `CompletableFuture` demo uses a named executor and propagates MDC
- [x] Correlation id is generated or propagated on inbound HTTP requests
- [x] No Kafka, RabbitMQ, security, Docker Compose or Kubernetes were introduced
- [x] Tests pass for the new engine and concurrency demos

## Suggested commits

```text
feat(execution): add bounded order queue and worker pool configuration
feat(execution): add asynchronous order processing engine and execution endpoints
feat(logging): add correlation id filter and MDC propagation to worker threads
feat(learning): add concurrency learning demos and endpoints
test(execution): add engine, controller and concurrency demo tests
docs(phase-05): add concurrency and processing engine documentation
docs(readme): update README for phase 5 execution flow
```
