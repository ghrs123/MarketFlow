# Phase 6 - FIX Message Engine

## What this phase delivers

Phase 6 adds a deliberately simplified FIX-style message engine. A
persisted MarketFlow order can be converted into a pipe-delimited String,
stored in PostgreSQL, retrieved later and explained tag by tag.

The implementation is educational. It does not implement a real FIX
session and does not use QuickFIX/J or another external FIX library.

## Architecture shape

```text
fix
|- api
|  |- FixController
|  |- FixMessageResponse
|  |- FixExplanationResponse
|  |- FixTagExplanationResponse
|  `- RawFixMessageRequest
|- application
|  |- FixMessageApplicationService
|  |- FixMessageGenerator
|  |- FixMessageParser
|  `- FixMessageExplainer
|- domain
|  |- FixMessage
|  |- FixMessageRepository
|  |- FixTag
|  `- FixTagExplanation
`- infrastructure.jpa
   |- FixMessageEntity
   |- FixMessageJpaRepositoryAdapter
   `- SpringDataFixMessageJpaRepository
```

The application service owns orchestration and transaction boundaries.
The domain defines the message and persistence port. PostgreSQL details
remain in the infrastructure adapter, while the controller handles only
HTTP binding and DTO mapping.

## Simulated message format

Example:

```text
8=FIX.4.4|35=D|49=MARKETFLOW|56=SIMULATED_BROKER|11={orderId}|55=AAPL|54=1|38=100|40=2|44=150.25|52=2026-01-15T10:30:00Z
```

Supported tags:

| Tag | Name | Meaning |
| --- | --- | --- |
| 8 | BeginString | Simulated protocol version |
| 35 | MsgType | `D` for New Order Single |
| 49 | SenderCompID | `MARKETFLOW` |
| 56 | TargetCompID | `SIMULATED_BROKER` |
| 11 | ClOrdID | MarketFlow order UUID |
| 55 | Symbol | Instrument symbol |
| 54 | Side | `1` BUY, `2` SELL |
| 38 | OrderQty | Order quantity |
| 40 | OrdType | `2` limit order |
| 44 | Price | Limit price |
| 52 | SendingTime | UTC generation timestamp |

## Key technical decisions

### Plain String generation

The generator uses `String.join` and explicit tags. This makes the mapping
from the `Order` aggregate visible and easy to discuss.

Trade-off: it is transparent and dependency-free, but it does not provide
the validation, session state or wire compatibility of QuickFIX/J.

### Printable pipe delimiter

Real FIX commonly uses SOH as the field delimiter. This phase uses `|` so
messages remain readable in logs, curl responses and documentation.

Trade-off: readability improves, but the output is not valid FIX wire data.

### One message per order

The database enforces `UNIQUE(order_id)`. Repeated generation returns
`409 Conflict`, keeping the operation deterministic before the idempotency
work planned for Phase 7.

### Persist raw payload

The exact generated String is stored instead of reconstructing it on reads.
That preserves the original sending time and supports later audit or event
publication.

### Strict parser

The parser rejects blank messages, non-numeric tags, malformed fields,
duplicates, a missing `35` tag and any begin string other than `FIX.4.4`.
Unknown numeric tags are retained and described as unknown by the explainer.

## Request lifecycle

### Generate and persist

1. Client calls `POST /orders/{id}/fix-message`.
2. The controller parses the UUID and delegates.
3. The service verifies the order exists.
4. The service rejects an already generated message.
5. The generator maps order data to the simulated FIX String.
6. The adapter persists the message and its timestamps.
7. A generation counter and timer are updated.
8. The API returns `201 Created` with a `Location` header.

### Retrieve and explain

1. The service verifies the order exists.
2. It loads the persisted message by `order_id`.
3. The parser creates an insertion-ordered tag map.
4. The explainer maps known tags to names and descriptions.
5. Unknown tags remain visible with `known=false`.

### Explain caller-provided raw data

1. Bean Validation bounds and rejects blank input.
2. The parser validates each `numericTag=value` field.
3. The explainer returns one response entry per tag.
4. The message is not persisted.

## Failure paths

- Missing order: `404 order-not-found`
- Existing order without generated message: `404 fix-message-not-found`
- Duplicate generation: `409 fix-message-already-exists`
- Blank or oversized request: `400 validation`
- Invalid FIX-like format: `400 invalid-fix-message`
- Unexpected internal failure: generic `500` without internal details

## Observability

Business log:

```text
Simulated FIX message generated for order {orderId}
```

Metrics:

```text
marketflow.fix.message.generated.success
marketflow.fix.message.generated.failure
marketflow.fix.message.explained.success
marketflow.fix.message.explained.failure
marketflow.fix.message.generation.duration
```

The existing correlation ID filter remains active for every endpoint.

## How to run

```bash
export DB_URL=jdbc:postgresql://localhost:5432/marketflow
export DB_USERNAME=marketflow
export DB_PASSWORD=marketflow

mvn spring-boot:run
```

## How to test

Full suite:

```bash
mvn clean test
```

Phase 6 only:

```bash
mvn "-Dtest=FixMessageGeneratorTest,FixMessageParserTest,FixMessageExplainerTest,FixMessageApplicationServiceTest,FixControllerTest,FixMessageRepositoryIntegrationTest" test
```

Docker must be running for PostgreSQL Testcontainers tests.

## curl examples

Create an order:

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "C001",
    "symbol": "AAPL",
    "side": "BUY",
    "quantity": 100,
    "price": 150.25
  }'
```

Generate:

```bash
curl -i -X POST http://localhost:8080/orders/{id}/fix-message
```

Retrieve:

```bash
curl -s http://localhost:8080/orders/{id}/fix-message | jq
```

Explain persisted tags:

```bash
curl -s http://localhost:8080/orders/{id}/fix-explanation | jq
```

Explain raw data:

```bash
curl -s -X POST http://localhost:8080/fix/explain \
  -H "Content-Type: application/json" \
  -d '{"rawMessage":"8=FIX.4.4|35=D|55=AAPL|54=1"}' | jq
```

## Simulation versus real FIX

This implementation omits:

- SOH wire delimiter
- `BodyLength(9)`
- `CheckSum(10)`
- `MsgSeqNum(34)`
- session logon and logout
- heartbeat and test requests
- resend requests and gap fill
- execution reports
- cancel request workflows
- field dictionaries and version negotiation

A production FIX integration would normally use a mature engine such as
QuickFIX/J and would require session persistence, sequence recovery,
counterparty certification and strict latency monitoring.

## Interview narrative

In Phase 6 I added a simulated FIX message engine without hiding the
mapping behind an external library. A persisted order is transformed into
a deterministic tag-based String, and BUY/SELL semantics are represented
using standard-looking side codes.

I separated generation, parsing and explanation because they solve
different problems. Generation maps domain state to a message, parsing
validates untrusted text into structured data, and explanation adds
educational metadata without changing the parsed values.

The raw message is persisted under a unique order constraint. This keeps
the original sending timestamp and makes duplicate generation explicit.
The persistence port remains in the domain and the JPA implementation stays
in infrastructure.

I would be explicit in an interview that this is not a production FIX
engine. It demonstrates message tags and boundary validation, while real
FIX requires session management, sequence numbers, checksums, recovery and
counterparty certification.

## Interview questions

1. What problem does FIX solve?
   - Standard electronic communication between financial counterparties.
2. What does `35=D` mean?
   - New Order Single.
3. Why are FIX messages tag-based?
   - Compact, ordered and dictionary-driven representation.
4. Why is BUY `54=1` and SELL `54=2`?
   - Standard side codes mirrored by this simulation.
5. Why persist the raw message?
   - Auditability, reproducibility and later publication.
6. Why reject duplicate tags?
   - Ambiguous values can produce unsafe interpretation.
7. Why preserve field order?
   - Readability and protocol-like ordering, even though this is simulated.
8. Why not build a real FIX engine manually?
   - Session and recovery semantics are complex and high risk.
9. What would QuickFIX/J add?
   - Dictionaries, sessions, sequence handling, persistence and callbacks.
10. What production concerns are still missing?
    - Certification, latency, recovery, security and operational monitoring.

## Exercises

1. Add a simulated Execution Report with `35=8`.
2. Add validation for known enumerated tag values.
3. Compare pipe and SOH delimiters in parser tests.
4. Add a checksum demonstration without claiming FIX compatibility.
5. Model a cancel request as a separate message type.

## Definition of Done

- [x] BUY generates `54=1`
- [x] SELL generates `54=2`
- [x] Generated messages are persisted
- [x] Generated messages can be retrieved by order
- [x] Raw messages are parsed into ordered fields
- [x] Tags return name, value and description
- [x] Unknown numeric tags remain visible
- [x] Invalid input returns RFC 7807 errors
- [x] PostgreSQL migration exists
- [x] Unit, controller and persistence tests exist
- [x] No external FIX library was introduced
- [x] No Kafka, security, Docker Compose or Kubernetes was introduced

## Suggested commits

```text
feat(fix): add simulated FIX domain and tag catalogue
feat(fix): add message generator parser and explainer
feat(fix): persist generated messages with Flyway
feat(fix): expose generation and explanation endpoints
test(fix): cover FIX engine and PostgreSQL persistence
docs(phase-06): document simulated FIX message engine
```
