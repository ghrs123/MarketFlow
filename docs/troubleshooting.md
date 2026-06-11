# MarketFlow Troubleshooting

## Start with one request

1. Obtain `X-Correlation-Id` from the response or client request.
2. Search structured logs for the matching `correlationId`.
3. Confirm the HTTP status and RFC 7807 `correlationId` on failures.
4. Follow `orderId` across creation, queue, retry, execution and audit logs.

## Health diagnosis

```bash
curl -s http://localhost:8081/actuator/health
curl -s http://localhost:8081/actuator/health/readiness
curl -s http://localhost:8080/monitoring/summary
```

- `database DOWN`: verify `DB_URL`, credentials, PostgreSQL availability and pool exhaustion.
- `orderQueue OUT_OF_SERVICE`: inspect queue size, active workers and processing latency.
- `deadLetterQueue warning=true`: inspect `/execution/dlq`; do not reprocess until the failure cause is understood.
- `processingEngine running=false`: start it through `POST /execution/start` when processing is expected.

## Metrics diagnosis

```bash
curl -s http://localhost:8081/actuator/prometheus
curl -s http://localhost:8081/actuator/metrics/marketflow.orders.created
```

- Rising queue with stable workers indicates throughput below arrival rate.
- Rising retries followed by DLQ growth indicates a persistent rather than transient failure.
- High processing p95 with normal queue size points to slow individual operations.
- High HTTP latency with normal business timers points to controller, serialization or infrastructure overhead.

## Common failure paths

### PostgreSQL unavailable

The database health indicator becomes `DOWN`; repository calls fail and the
global exception handler returns a generic 500 without exposing connection
details.

### Queue full

New enqueue requests return 503. Existing queued work remains available to
workers. Reduce arrival rate or diagnose worker throughput before increasing
capacity.

### Repeated processing failure

The engine applies capped exponential backoff. After the final attempt the order
is marked failed, added to the in-memory DLQ and exposed in metrics and audit
logs.

### Missing correlation ID

The filter generates a UUID and returns it as `X-Correlation-Id`. Invalid IDs
are replaced to prevent unbounded or control-character values entering logs.

## Resilience diagnosis

Inspect all Resilience4j signals:

```bash
curl -s http://localhost:8081/actuator/prometheus \
  | grep resilience4j
```

### Broker circuit breaker open

Symptoms:

- broker requests return `PENDING_BROKER_RECOVERY`
- response contains `fallback=true`
- `resilience4j_circuitbreaker_state{name="broker",state="open"}` becomes `1`

Use an order with a normal symbol to verify recovery after the configured
five-second open-state wait. `BROKER_FAIL` and `BROKER_TIMEOUT` intentionally
keep the simulated dependency unhealthy.

### FIX retry exhausted

`FIX_RETRY` fails twice and should succeed on the third attempt. `FIX_FAIL`
always fails and returns HTTP `503` with the RFC 7807 type
`external-service-unavailable`.

Check `resilience4j_retry_calls` before changing retry counts. Increasing
attempts can amplify dependency load and database work.

### Order creation rate limited

More than five `POST /orders` requests in one second return HTTP `429`.
Inspect `resilience4j_ratelimiter_available_permissions`. Clients should
apply bounded backoff instead of immediately retrying in a tight loop.

### Processing bulkhead saturated

The order-processing bulkhead permits four concurrent calls. Rejected work is
marked failed with outcome `Processing capacity exhausted`. Compare bulkhead
capacity, active workers, queue size and processing latency before increasing
the limit.
