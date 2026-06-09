# Phase 4 - Data Structures and In-Memory Order Book

## What this phase delivers

Phase 4 adds an in-memory order book on top of the persisted order API built in the earlier phases. Orders are still created and stored through the existing PostgreSQL-backed flow, but they can now also be loaded into a process-local order book for price-ordered inspection.

This phase introduces three focused capabilities: add an existing order to the in-memory book, inspect the current buy and sell sides, and read a recent-order cache entry by id. It also adds learning endpoints that explain the data-structure choices behind the implementation.

## Data structures used and why

### `PriorityQueue`

Used in `OrderBook` for BUY and SELL sides.

- BUY side: highest price first, then earliest `createdAt`
- SELL side: lowest price first, then earliest `createdAt`

Why it fits:

- the main operation is retrieving the best price on each side
- heap semantics are simpler than a tree structure for this phase
- it matches the didactic goal in `MARKETFLOW.md`

### `ConcurrentHashMap`

Used in `OrderBook` as a direct index by `orderId`.

Why it fits:

- supports concurrent membership checks
- prevents duplicate insertion into the in-memory book
- provides fast direct lookup without scanning a queue

### `LinkedHashMap` in access-order mode

Used in `RecentOrderCache`.

Why it fits:

- implements bounded LRU behaviour with little code
- deterministic eviction is easy to explain and test
- enough for a local, single-process cache in this phase

## Technical decisions

### Why `PriorityQueue` instead of `ConcurrentSkipListMap`

`PriorityQueue` was chosen because the phase is about learning the right structure for best-price retrieval with the smallest amount of machinery. The order book needs a head element more often than arbitrary range scans, so a heap is the clearer choice here.

`ConcurrentSkipListMap` would give stronger sorted-map traversal and concurrent ordering semantics, but it introduces more conceptual weight than this phase needs. That trade-off is better postponed to the concurrency-focused work in later phases.

### Synchronization strategy

`PriorityQueue` is not thread-safe. The implementation therefore uses:

- `ConcurrentHashMap` for direct concurrent membership access
- explicit `synchronized` blocks in `OrderBook` to keep queue mutations and queue reads consistent

That split is intentional. The map solves key-based concurrency, but it does not protect the invariant that the queues and the index must stay aligned. That invariant is owned by `OrderBook` itself.

## Flow: `POST /orders/{id}/book` to snapshot

1. Client creates an order through the existing `POST /orders` flow.
2. The order is persisted in PostgreSQL and remains the source of truth.
3. Client calls `POST /orders/{id}/book`.
4. `OrderBookController` validates the path UUID and delegates to `OrderBookApplicationService`.
5. `OrderBookApplicationService` loads the persisted order via `OrderApplicationService.findById(...)`.
6. The service creates an `OrderTask` using `OrderTask.from(order)`.
7. The task is inserted into `OrderBook`.
8. If the `orderId` already exists, `OrderAlreadyInBookException` is raised and mapped to `409 Conflict`.
9. On successful insert, the same `OrderTask` is added to `RecentOrderCache`.
10. `GET /order-book` returns a snapshot composed from copied queue state, grouped into BUY and SELL lists plus counts and timestamp.

## Interview narrative

In Phase 4 I added an in-memory order book without changing the persistence model built in the previous phases. The key design choice was to keep PostgreSQL as the source of truth for orders while introducing a separate in-memory read structure for price ordering and recent access patterns.

I modelled the order book with two `PriorityQueue` instances, one for BUY and one for SELL, because the main operation is retrieving the best price on each side. I paired that with a `ConcurrentHashMap` for direct lookup and deduplication, and with a bounded `LinkedHashMap` cache for recent orders. That gives each structure a single clear responsibility.

The important engineering trade-off is concurrency control. `PriorityQueue` is not thread-safe, so I did not pretend `ConcurrentHashMap` solves everything. The implementation synchronizes queue access inside `OrderBook` to protect the invariant between the queues and the direct index. That keeps the phase technically honest without dragging in the heavier concurrency model planned for later work.

## Interview questions and answer skeletons

1. Why use two `PriorityQueue` instances in an order book?
   Answer skeleton: separate BUY and SELL semantics, efficient head retrieval, simple comparator-based ordering.

2. Why is `ConcurrentHashMap` still needed if the queues already store the orders?
   Answer skeleton: direct lookup by id, deduplication, avoid linear scans, concurrent membership access.

3. Why not persist the order book in this phase?
   Answer skeleton: phase scope is data structures, keep storage concerns separate, persisted order remains source of truth.

4. Why choose `PriorityQueue` over `ConcurrentSkipListMap`?
   Answer skeleton: best-price retrieval is the primary operation, simpler mental model, defer richer concurrent ordering to later phases.

5. What concurrency problem remains even with `ConcurrentHashMap`?
   Answer skeleton: cross-structure invariant between map and queues, queue is not thread-safe, need explicit synchronization.

6. Why copy the queues when building a snapshot?
   Answer skeleton: avoid mutating live heap, preserve ordering, return stable read model.

7. How does the recent-order cache implement LRU?
   Answer skeleton: `LinkedHashMap` access-order mode, bounded capacity, eldest removal on insert.

8. Why convert `Order` into `OrderTask`?
   Answer skeleton: separate persisted aggregate from in-memory representation, keep order-book state lean and purpose-built, no separate mapper class needed.

9. What does `409 Conflict` mean in `POST /orders/{id}/book`?
   Answer skeleton: client requested a state transition that conflicts with current in-memory state because the order is already present.

10. What would you revisit first in a higher-concurrency version?
    Answer skeleton: queue synchronization strategy, lock contention, possibly tree-based concurrent structures or partitioned books.

## Definition of Done

- [x] Order book is in-memory only
- [x] BUY side is ordered by highest price first
- [x] SELL side is ordered by lowest price first
- [x] Duplicate order insertion is rejected with `409 Conflict`
- [x] Recent order cache supports bounded LRU eviction
- [x] Order book endpoints are exposed under `/orders/{id}/book` and `/order-book`
- [x] Learning endpoints explain the data-structure choices
- [x] No Kafka, security, Docker Compose or Kubernetes changes were introduced
- [x] Unit and controller tests cover the new behaviour
- [x] The full test suite passes across phases 1 to 4

## Suggested commits

```text
feat(order-book): add in-memory order book and recent cache structures
feat(order-book): add order book application service and REST endpoints
test(order-book): add unit and controller coverage for the in-memory order book
docs(phase-04): add phase documentation and learning endpoints
docs(readme): update repository guide for phase 4 order book features
```
