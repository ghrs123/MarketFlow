# Phase 1 - Foundation + Order API (in-memory)

## What this phase delivers

A complete vertical slice for order management: HTTP request comes in,
goes through validation and an application service, lands in an in-memory
repository, and produces a typed HTTP response. Errors are translated by
a single `@RestControllerAdvice` into RFC 7807 `ProblemDetail` payloads.

This is the only phase where persistence is deliberately in-memory. The
goal is to nail Spring Core, REST design, Bean Validation and exception
handling before adding JPA, transactions and databases in Phase 2.

## Why start in-memory?

Three reasons:

1. **Tight feedback loop.** No external dependency means the lab boots in
   under three seconds and tests run in milliseconds.
2. **Forces the right seams.** Because the service depends on a
   `OrderRepository` interface (the domain port), swapping in a JPA
   adapter later is a one-class change. If we had started with JPA, the
   service would have been tempted to depend on `JpaRepository` directly.
3. **Separates concerns to learn.** Each topic gets isolated attention:
   first DI / REST / validation, then persistence, then concurrency, etc.

## What is a vertical slice?

A unit of work that touches every architectural layer for one feature.
Phase 1 ships create / fetch / list end-to-end. That is more valuable than
finishing one layer (e.g. all DTOs) across all features, because it
proves the wiring works and exposes the real friction points early.

## Architectural shape

```
api (HTTP)        -> OrderController, CreateOrderRequest, OrderResponse
application       -> OrderApplicationService
domain            -> Order, OrderSide, OrderStatus, OrderRepository (port)
infrastructure    -> OrderInMemoryRepository (adapter)
shared/exception  -> GlobalExceptionHandler, OrderNotFoundException
```

The domain depends on nothing. The application depends on the domain.
The infrastructure adapter implements the domain port. The API layer
depends on the application. This is a small Hexagonal layout - the
boundary that matters is `OrderRepository`.

## Key technical decisions

### 1. Constructor injection
Dependencies are `final` and validated at startup. No reflection-based
field injection, no setter injection. Tests instantiate the class with
plain `new`, no Spring context required.

### 2. Records for DTOs
`CreateOrderRequest` and `OrderResponse` are records: immutable, no
boilerplate, equality by component. They are the public contract; the
domain `Order` is internal.

### 3. RFC 7807 ProblemDetail
Every error response is a `ProblemDetail` (`application/problem+json`)
with `type`, `title`, `status`, `detail`, `instance` and extension
properties. One contract, standardised, machine-readable. Internal
exception messages and stack traces are never returned to clients.

### 4. ConcurrentHashMap as the in-memory store
Tomcat serves requests from a thread pool. A plain `HashMap` would race
on writes; `Collections.synchronizedMap` would serialize all access.
`ConcurrentHashMap` uses bucket-level locking on writes and lock-free
reads. The semantics also match what Phase 5 (the processing engine)
will need.

### 5. BigDecimal for money and quantity
Binary floating point cannot represent `0.1` exactly. In finance that is
not a rounding concern, it is a correctness bug. `BigDecimal` is the
right type; `@Digits(integer = 18, fraction = 8)` bounds the precision
to something we can persist later.

### 6. Instant for timestamps
UTC by construction, no time-zone ambiguity, serializable as ISO-8601.

### 7. UUID for identifiers
Server-generated, opaque to the client, safe to expose, no information
leak about sequence or volume.

## Request lifecycle

```
HTTP request
  -> DispatcherServlet
  -> OrderController.create(@Valid @RequestBody CreateOrderRequest)
       Jackson deserializes JSON into the record
       Bean Validation runs the constraints
       On failure -> MethodArgumentNotValidException -> GlobalExceptionHandler -> 400
  -> OrderApplicationService.createOrder(...)
       Order.createNew(...)    domain factory
       orderRepository.save(o) outbound port
  -> OrderController returns ResponseEntity.created(location).body(OrderResponse.from(saved))
  -> Jackson serializes back to JSON
```

## How to test manually

Application:

```bash
mvn spring-boot:run
```

Create:

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"clientId":"C001","symbol":"AAPL","side":"BUY","quantity":100,"price":150.25}'
```

Validation error (400 + ProblemDetail with `errors` array):

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"clientId":"","symbol":"AAPL","side":"BUY","quantity":-1,"price":0}'
```

Not found (404 + ProblemDetail):

```bash
curl -i http://localhost:8080/orders/00000000-0000-0000-0000-000000000000
```

Health:

```bash
curl -s http://localhost:8080/health/custom | jq
curl -s http://localhost:8080/actuator/health | jq
```

## How to test automatically

```bash
mvn test
```

Covers:

- Context boot (`MarketFlowApplicationTests`).
- Service behaviour and `OrderNotFoundException` (`OrderApplicationServiceTest`).
- Controller happy paths, validation 400, malformed JSON 400, not-found
  404, list (`OrderControllerTest`).

## Risks and trade-offs accepted in this phase

- State is lost on restart. Acceptable: persistence is Phase 2.
- `findAll()` is unbounded. Acceptable for a study lab; paginated in
  Phase 2.
- No security. Endpoints are open. Security is Phase 10.
- No correlation id / structured logging. Added in Phase 9.

## How to talk about this phase in an interview

> "I built a small order management slice in Spring Boot 3 with Java 21.
> The point of this slice was to get Spring Core, REST, validation and
> error handling right before adding persistence. I depend on an
> `OrderRepository` port from the domain layer, so the application
> service has no idea whether it is talking to a `ConcurrentHashMap` or
> to a JPA repository - that swap is a single class in Phase 2.
>
> The error contract is RFC 7807 `ProblemDetail`, which Spring 6 supports
> natively. I never let exception messages or stack traces leak into HTTP
> responses; the catch-all handler logs server-side and returns a generic
> 500. Validation failures are translated into a structured `errors`
> array on the problem document.
>
> I used `BigDecimal` for price and quantity rather than `double` - that
> is non-negotiable in any financial domain. Identifiers are server-side
> UUIDs and timestamps are `Instant` to keep things in UTC."

## Interview questions this phase prepares you for

### Spring Core / DI

1. What is the `ApplicationContext`?
2. Difference between `@Component`, `@Service`, `@Repository`,
   `@RestController`. Why does `@Repository` exist if it does the same as
   `@Component`?
3. Constructor vs setter vs field injection - which and why?
4. What happens if you have two beans of the same type? How do you
   disambiguate?
5. What is the default bean scope? When would you use prototype?
6. What is `@ConfigurationProperties` and how does it differ from
   `@Value`?

### REST design

1. Why return 201 and not 200 for `POST`?
2. What does the `Location` header do, and why is it useful?
3. Why not expose the JPA entity directly in the response?
4. When would you use PUT vs PATCH?
5. How do you version a REST API?

### Bean Validation

1. What does `@Valid` actually do?
2. Difference between `@NotNull`, `@NotEmpty`, `@NotBlank`.
3. How would you write a custom constraint?
4. What is `@Validated` (on the class) vs `@Valid` (on the parameter)?
5. How do you validate method parameters that are not inside a
   `@RequestBody`?

### Exception handling

1. What is `@RestControllerAdvice` vs `@ControllerAdvice`?
2. What is RFC 7807 and why use `ProblemDetail`?
3. How do you avoid leaking internal exception messages?
4. How does Spring choose which `@ExceptionHandler` to invoke when
   multiple match?
5. Why should you not catch `Throwable` in a global handler?

### Java basics that show up immediately

1. Why is `BigDecimal` preferred over `double` for money?
2. Why is `Instant` preferred over `Date` and `LocalDateTime` for stored
   timestamps?
3. Why are records a good fit for DTOs?
4. `HashMap` vs `ConcurrentHashMap` - what breaks if you use `HashMap`
   in a multi-threaded controller?
5. What does `Objects.requireNonNull` give you over plain `!= null`?

## Exercises before moving to Phase 2

1. Add a `GET /orders?status=NEW&clientId=C001` filter. Decide where the
   filtering lives (controller, service, repository).
2. Add a custom Bean Validation constraint `@ValidSymbol` that enforces
   uppercase letters of length 1-6. Make the violation message land in
   the `errors` array.
3. Add an `OrderAlreadyExistsException` mapped to 409 and write a test
   that drives it.
4. Implement a `delete(id)` use case that throws if the order is not in
   `NEW` status. Cover the happy and unhappy paths in tests.
5. Read Spring's source for `ProblemDetail` and write a one-paragraph
   note on how `setProperty(...)` is serialized.

## Definition of Done for this phase

- [x] Application boots cleanly on `mvn spring-boot:run`.
- [x] `POST /orders` returns 201 with the new resource and a `Location`
      header.
- [x] `GET /orders/{id}` returns 200 or 404.
- [x] `GET /orders` returns the list snapshot.
- [x] Bean Validation returns 400 with an RFC 7807 body and the field
      errors.
- [x] Malformed JSON returns 400 with an RFC 7807 body.
- [x] No internal exception messages or stack traces leak to clients.
- [x] `mvn test` passes (11 tests).
- [x] `README.md` and this document are up to date.

## Suggested commits

```
chore: initialize Spring Boot project
feat: add order domain model and in-memory repository port
feat: add in-memory order repository adapter
feat: add order application service
feat: add order REST API with validation
feat: add global exception handler with RFC 7807 ProblemDetail
feat: add custom health and learning endpoints
test: add application context, service and controller tests
docs: add phase 01 documentation and README
```
