# MarketFlow Alert Guide

This phase prepares alert definitions but does not provision an alert manager.
Thresholds are initial operational hypotheses and must be tuned with production
traffic and service-level objectives.

## Recommended alerts

| Signal | Initial condition | Severity | Response |
| --- | --- | --- | --- |
| Readiness | `/actuator/health/readiness` is not `UP` for 2 minutes | Critical | Check database and order queue indicators |
| Failure ratio | Failed order rate exceeds 5% of processed orders for 10 minutes | Warning | Inspect failure events, symbols and downstream state |
| DLQ backlog | `marketflow_dead_letter_queue_size > 0` for 15 minutes | Warning | Inspect `/execution/dlq` and fix the failure before reprocessing |
| Queue saturation | Queue size exceeds 80% of configured capacity for 10 minutes | Warning | Check worker utilization and processing latency |
| Worker loss | Active workers are below configured workers while the engine is running | Critical | Inspect worker exceptions and restart the engine if required |
| Processing latency | p95 exceeds the agreed SLO for 10 minutes | Warning | Correlate queue, CPU, GC and database metrics |
| HTTP errors | 5xx request ratio exceeds 1% for 5 minutes | Critical | Search structured logs by correlation ID |

## Alert design notes

- Alert on sustained symptoms, not individual failures.
- Keep readiness alerts separate from business degradation alerts.
- DLQ presence is a warning because inspection and reprocessing remain available.
- Never include order payloads, credentials or client data in alert labels.
