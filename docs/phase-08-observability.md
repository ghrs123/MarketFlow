# Phase 8 - Observability

## What this phase delivers

MarketFlow now exposes a complete process-local observability layer:
correlated structured logs, audit records, Micrometer business metrics,
Prometheus output, custom health indicators, an operational summary and a
Grafana dashboard definition.

The curriculum currently labels this capability as Phase 9 because its Phase 8
introduces external messaging. The project instruction explicitly skips real
Kafka and delivers observability as Phase 8, so this branch intentionally uses
`feature/08-observability`.

## Architecture

1. `CorrelationIdFilter` validates or generates `X-Correlation-Id`, places it in
   MDC, records HTTP duration and returns the identifier to the client.
2. `MdcTaskDecorator` captures the submitting thread context and restores it
   around work executed by named application-managed pools.
3. Queue messages retain their captured MDC because workers are long-running
   consumers whose task-level context alone cannot identify each order.
4. `OrderMetricsService` owns stable business counter and timer names.
5. `AuditLogService` emits order lifecycle facts through `MARKETFLOW_AUDIT`.
6. Actuator aggregates database, queue, DLQ and processing-engine indicators.
7. `/monitoring/summary` exposes a concise application-owned operational view.

## Technical decisions and trade-offs

- Logs use a Logback JSON-like pattern without adding a JSON encoder dependency.
  The format is structured and stdout-friendly, but a production deployment
  would normally use a strict JSON encoder.
- The DLQ indicator reports `UP` plus `warning=true`. A non-empty DLQ is business
  degradation, not loss of the reprocessing dependency.
- The processing-engine indicator reports its state without taking readiness
  down when workers are deliberately stopped.
- HTTP metrics use bounded method and status tags. Correlation and order IDs are
  never metric tags because their cardinality is unbounded.
- Prometheus and Grafana files are configuration artefacts only. Docker Compose,
  Kubernetes and external brokers remain outside this phase.

## Run

```bash
export DB_URL=jdbc:postgresql://localhost:5432/marketflow
export DB_USERNAME=marketflow
export DB_PASSWORD=marketflow
mvn spring-boot:run
```

## Demonstrate

```bash
curl -i -H "X-Correlation-Id: phase-08-demo" http://localhost:8081/actuator/health
curl -s http://localhost:8081/actuator/prometheus
curl -s http://localhost:8080/monitoring/summary
curl -s http://localhost:8080/learning/logging
curl -s http://localhost:8080/learning/monitoring
curl -s http://localhost:8080/learning/performance/jvm
```

## Test

```bash
mvn test
mvn package
```

## Interview narrative

The observability design starts at the request boundary. Every request has a
validated correlation ID, and that context follows work into named executors
through a `TaskDecorator`. Long-running queue consumers additionally restore
the MDC captured with each message, preventing context leakage between orders.

Business metrics are centralized so metric contracts do not drift between
services. Counters describe outcomes, timers measure latency and low-cardinality
gauges expose resource pressure. Health indicators answer dependency
availability, while the monitoring summary provides a stable operational API.

The design separates availability from degradation. A full queue affects
readiness because new work is rejected; a non-empty DLQ is a warning because the
service can still inspect and reprocess failed orders. This distinction avoids
restart loops caused by business failures.

## Interview questions

1. Why does MDC require explicit propagation across executors?
2. Why is a `TaskDecorator` insufficient by itself for a long-running queue consumer?
3. What makes a metric label dangerous for Prometheus cardinality?
4. How do counters, gauges and timers model different operational questions?
5. Why should a DLQ backlog not necessarily make liveness fail?
6. What is the difference between liveness and readiness?
7. How do RED and USE metrics apply to this service?
8. Why return correlation ID in RFC 7807 errors?
9. What are the trade-offs of JSON-pattern logs versus a JSON encoder?
10. How would this process-local design evolve for distributed tracing?

## Definition of done

- [x] Correlation ID is present in response headers, logs and errors.
- [x] MDC propagates through managed executors and queue messages.
- [x] Structured business, technical, error and audit logs exist.
- [x] Micrometer counters, gauges and timers are exposed to Prometheus.
- [x] Database, queue, DLQ and engine health indicators exist.
- [x] Operational summary and learning endpoints exist.
- [x] Prometheus and Grafana configuration artefacts exist.
- [x] Alert and troubleshooting guides exist.
- [x] Full Maven test suite passes (134 tests, 0 failures, 0 errors).
