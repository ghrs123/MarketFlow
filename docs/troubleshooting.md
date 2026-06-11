# MarketFlow Troubleshooting

## Start with one request

1. Obtain `X-Correlation-Id` from the response or client request.
2. Search structured logs for the matching `correlationId`.
3. Confirm the HTTP status and RFC 7807 `correlationId` on failures.
4. Follow `orderId` across creation, queue, retry, execution and audit logs.

## Health diagnosis

```bash
curl -s http://localhost:8080/actuator/health
curl -s http://localhost:8080/actuator/health/readiness
curl -s http://localhost:8080/monitoring/summary
```

- `database DOWN`: verify `DB_URL`, credentials, PostgreSQL availability and pool exhaustion.
- `orderQueue OUT_OF_SERVICE`: inspect queue size, active workers and processing latency.
- `deadLetterQueue warning=true`: inspect `/execution/dlq`; do not reprocess until the failure cause is understood.
- `processingEngine running=false`: start it through `POST /execution/start` when processing is expected.

## Metrics diagnosis

```bash
curl -s http://localhost:8080/actuator/prometheus
curl -s http://localhost:8080/actuator/metrics/marketflow.orders.created
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
