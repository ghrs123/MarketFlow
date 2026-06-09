package com.gustavo.marketflow.learning;

import com.gustavo.marketflow.execution.application.ExecutionProperties;
import com.gustavo.marketflow.learning.concurrency.AtomicCounterDemo;
import com.gustavo.marketflow.learning.concurrency.CompletableFutureDemo;
import com.gustavo.marketflow.learning.concurrency.CompletableFutureDemoResult;
import com.gustavo.marketflow.learning.concurrency.DeadlockDemo;
import com.gustavo.marketflow.learning.concurrency.RaceConditionDemo;
import com.gustavo.marketflow.learning.concurrency.RaceConditionDemoResult;
import com.gustavo.marketflow.learning.concurrency.ReentrantLockCounterDemo;
import com.gustavo.marketflow.learning.concurrency.StarvationDemo;
import com.gustavo.marketflow.learning.concurrency.SynchronizedCounterDemo;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only endpoints that expose concepts introduced in the current phase
 * and retained from earlier phases.
 *
 * <p>Not part of the product surface - they exist so the lab can be
 * explored from a browser or curl and the same concepts can be retold in
 * an interview without having to dig through code.</p>
 *
 * <p>{@link ApplicationContext} is injected to demonstrate that the
 * container is a regular bean: it is available for inspection at runtime
 * just like any other dependency.</p>
 */
@RestController
@RequestMapping("/learning")
public class LearningController {

    private final ApplicationContext applicationContext;
    private final String activeProfile;
    private final RaceConditionDemo raceConditionDemo;
    private final SynchronizedCounterDemo synchronizedCounterDemo;
    private final AtomicCounterDemo atomicCounterDemo;
    private final ReentrantLockCounterDemo reentrantLockCounterDemo;
    private final DeadlockDemo deadlockDemo;
    private final StarvationDemo starvationDemo;
    private final CompletableFutureDemo completableFutureDemo;
    private final ExecutionProperties executionProperties;

    public LearningController(ApplicationContext applicationContext,
                              @Value("${spring.profiles.active:default}") String activeProfile,
                              RaceConditionDemo raceConditionDemo,
                              SynchronizedCounterDemo synchronizedCounterDemo,
                              AtomicCounterDemo atomicCounterDemo,
                              ReentrantLockCounterDemo reentrantLockCounterDemo,
                              DeadlockDemo deadlockDemo,
                              StarvationDemo starvationDemo,
                              CompletableFutureDemo completableFutureDemo,
                              ExecutionProperties executionProperties) {
        this.applicationContext = applicationContext;
        this.activeProfile = activeProfile;
        this.raceConditionDemo = raceConditionDemo;
        this.synchronizedCounterDemo = synchronizedCounterDemo;
        this.atomicCounterDemo = atomicCounterDemo;
        this.reentrantLockCounterDemo = reentrantLockCounterDemo;
        this.deadlockDemo = deadlockDemo;
        this.starvationDemo = starvationDemo;
        this.completableFutureDemo = completableFutureDemo;
        this.executionProperties = executionProperties;
    }

    @GetMapping("/spring/beans")
    public Map<String, Object> springBeans() {
        String[] all = applicationContext.getBeanDefinitionNames();
        List<String> marketflowBeans = Arrays.stream(all)
                .filter(name -> name.toLowerCase().contains("order")
                        || name.toLowerCase().contains("marketflow")
                        || name.toLowerCase().contains("learning")
                        || name.toLowerCase().contains("health")
                        || name.toLowerCase().contains("exception"))
                .sorted()
                .toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("topic", "Spring DI / ApplicationContext");
        body.put("activeProfile", activeProfile);
        body.put("totalBeanCount", all.length);
        body.put("applicationBeans", marketflowBeans);
        body.put("notes", List.of(
                "ApplicationContext is the IoC container that wires beans together.",
                "Constructor injection makes dependencies explicit and final.",
                "@Component, @Service, @Repository and @RestController are all specializations of @Component."
        ));
        return body;
    }

    @GetMapping("/rest")
    public Map<String, Object> rest() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("topic", "REST API design");
        body.put("principles", List.of(
                "Resources are identified by URIs.",
                "HTTP verbs encode the action: POST creates, GET reads, PUT/PATCH updates, DELETE removes.",
                "Status codes encode the outcome: 2xx success, 4xx client error, 5xx server error.",
                "On creation return 201 with a Location header to the new resource.",
                "Never expose internal entities directly - use DTOs for the public contract."
        ));
        body.put("endpointsInCurrentPhase", List.of(
                "POST /orders",
                "GET  /orders/{id}",
                "GET  /orders",
                "GET  /orders/{id}/history"
        ));
        return body;
    }

    @GetMapping("/validation")
    public Map<String, Object> validation() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("topic", "Bean Validation (Jakarta Validation)");
        body.put("howItWorks", List.of(
                "@Valid on a controller argument triggers validation of its constraints.",
                "Constraint violations throw MethodArgumentNotValidException.",
                "GlobalExceptionHandler maps that to a 400 Bad Request with a RFC 7807 ProblemDetail body."
        ));
        body.put("constraintsUsed", List.of(
                "@NotBlank: rejects null, empty and whitespace-only strings.",
                "@NotNull: rejects nulls.",
                "@DecimalMin(\"0.0\", inclusive = false): enforces strictly positive numbers.",
                "@Digits(integer, fraction): bounds numeric precision."
        ));
        return body;
    }

    @GetMapping("/exception-handling")
    public Map<String, Object> exceptionHandling() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("topic", "Centralised exception handling");
        body.put("strategy", List.of(
                "@RestControllerAdvice + @ExceptionHandler centralise translation from exception to HTTP.",
                "Use RFC 7807 ProblemDetail as the single error contract.",
                "Map domain exceptions (e.g. OrderNotFoundException) to specific status codes.",
                "Map framework exceptions (MethodArgumentNotValidException, HttpMessageNotReadableException) to 400.",
                "Catch-all Exception handler logs server-side and returns a generic 500 without leaking internals."
        ));
        body.put("knownErrorTypes", List.of(
                "https://marketflow.local/errors/order-not-found",
                "https://marketflow.local/errors/validation",
                "https://marketflow.local/errors/malformed-request",
                "https://marketflow.local/errors/invalid-argument",
                "https://marketflow.local/errors/internal"
        ));
        return body;
    }

        @GetMapping("/jpa/lazy-vs-eager")
        public Map<String, Object> jpaLazyVsEager() {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("topic", "JPA: LAZY vs EAGER");
                body.put("notes", List.of(
                                "Default to LAZY and fetch explicitly for the current use case.",
                                "EAGER often causes hidden query cost and over-fetching.",
                                "Use fetch joins or dedicated projections to avoid N+1 in read paths."
                ));
                return body;
        }

        @GetMapping("/jpa/n-plus-one")
        public Map<String, Object> jpaNPlusOne() {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("topic", "JPA: N+1 query problem");
                body.put("notes", List.of(
                                "N+1 appears when one query loads parents and additional queries load each child.",
                                "Detect through SQL logs and query counts in integration tests.",
                                "Mitigate using fetch join, @EntityGraph or optimized projection queries."
                ));
                return body;
        }

        @GetMapping("/transactions")
        public Map<String, Object> transactions() {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("topic", "Spring transactions");
                body.put("notes", List.of(
                                "Put @Transactional on service methods, not on controllers.",
                                "One order creation should persist order + history atomically.",
                                "Runtime exceptions trigger rollback by default."
                ));
                return body;
        }

        @GetMapping("/transactions/self-invocation")
        public Map<String, Object> transactionsSelfInvocation() {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("topic", "@Transactional self-invocation");
                body.put("notes", List.of(
                                "Spring applies @Transactional through proxies.",
                                "Calling a @Transactional method from inside the same class bypasses the proxy.",
                                "Extract transactional logic to another bean when proxy interception is required."
                ));
                return body;
        }

        @GetMapping("/hibernate/dirty-checking")
        public Map<String, Object> hibernateDirtyChecking() {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("topic", "Hibernate dirty checking");
                body.put("notes", List.of(
                                "Managed entities are tracked inside the persistence context.",
                                "Field changes are detected and flushed on commit.",
                                "Use transactional boundaries deliberately to control when changes are persisted."
                ));
                return body;
        }

        @GetMapping("/data-structures")
        public Map<String, Object> dataStructures() {
                return learningTopic(
                                "Data structures in the order book phase",
                                "PriorityQueue + LinkedHashMap + ConcurrentHashMap",
                                "Each structure serves a different access pattern: price ordering, recent-entry eviction and concurrent key lookup.",
                                List.of(
                                                "The design is intentionally in-memory, so process restarts lose state.",
                                                "Multiple structures increase coordination cost because invariants must stay aligned.",
                                                "The chosen structures optimize read and ordering behaviour, not persistence durability."
                                ),
                                List.of(
                                                "Why is one structure rarely enough for a non-trivial in-memory workflow?",
                                                "How do access patterns drive data-structure choice?",
                                                "What invariants must be protected when several structures model the same data?"
                                ));
        }

        @GetMapping("/data-structures/order-book")
        public Map<String, Object> orderBook() {
                return learningTopic(
                                "Order book price ordering",
                                "PriorityQueue",
                                "The order book needs efficient access to the best BUY and SELL prices, and PriorityQueue gives direct heap-based access to the head element.",
                                List.of(
                                                "PriorityQueue is not thread-safe, so queue access must be synchronized.",
                                                "It is efficient for head access but not for arbitrary sorted traversal without copying.",
                                                "Removing or updating arbitrary entries is weaker than a tree-based structure."
                                ),
                                List.of(
                                                "Why use PriorityQueue for best-price retrieval?",
                                                "How do you model BUY and SELL ordering differently?",
                                                "What are the concurrency implications of PriorityQueue?"
                                ));
        }

        @GetMapping("/data-structures/cache")
        public Map<String, Object> cache() {
                return learningTopic(
                                "Recent order cache",
                                "LinkedHashMap (access-order mode)",
                                "LinkedHashMap in access-order mode gives simple LRU semantics with bounded size and deterministic eviction behaviour.",
                                List.of(
                                                "LinkedHashMap is not thread-safe, so access must be synchronized.",
                                                "It is simple and fast for this phase, but not distributed or persistent.",
                                                "Eviction is capacity-based only; there is no TTL or weight-based policy."
                                ),
                                List.of(
                                                "How does access-order LinkedHashMap implement LRU?",
                                                "When is an in-memory LRU cache sufficient?",
                                                "What would change if cache entries needed TTL or distribution?"
                                ));
        }

        @GetMapping("/data-structures/concurrent-map")
        public Map<String, Object> concurrentMap() {
                return learningTopic(
                                "Concurrent direct lookup",
                                "ConcurrentHashMap",
                                "The order book needs lock-safe membership and direct access by orderId, and ConcurrentHashMap provides scalable concurrent reads and updates.",
                                List.of(
                                                "ConcurrentHashMap handles key-level concurrency but does not protect cross-structure invariants by itself.",
                                                "It is excellent for lookup and deduplication, but it does not maintain sorted order.",
                                                "You still need explicit coordination when map state must stay consistent with queues."
                                ),
                                List.of(
                                                "Why use ConcurrentHashMap for deduplication?",
                                                "What problems does ConcurrentHashMap solve and what does it not solve?",
                                                "Why is a concurrent map not enough to build a full order book?"
                                ));
        }

        private Map<String, Object> learningTopic(String topic,
                                                  String structure,
                                                  String whyThisStructure,
                                                  List<String> tradeOffs,
                                                  List<String> interviewTopics) {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("topic", topic);
                body.put("structure", structure);
                body.put("whyThisStructure", whyThisStructure);
                body.put("tradeOffs", tradeOffs);
                body.put("interviewTopics", interviewTopics);
                return body;
        }

        @GetMapping("/concurrency/race-condition")
        public Map<String, Object> raceCondition() {
                RaceConditionDemoResult unsafe = raceConditionDemo.run(8, 2_000);
                RaceConditionDemoResult sync = synchronizedCounterDemo.run(8, 2_000);
                RaceConditionDemoResult atomic = atomicCounterDemo.run(8, 2_000);
                RaceConditionDemoResult lock = reentrantLockCounterDemo.run(8, 2_000);

                Map<String, Object> body = new LinkedHashMap<>();
                body.put("topic", "Race condition");
                body.put("unsafe", unsafe);
                body.put("synchronized", sync);
                body.put("atomic", atomic);
                body.put("reentrantLock", lock);
                body.put("tradeOffs", List.of(
                                "Unsynchronized mutation is fast but incorrect under contention.",
                                "synchronized is simple but can serialize access more aggressively.",
                                "AtomicInteger is efficient for counters but does not replace locks for compound invariants."
                ));
                return body;
        }

        @GetMapping("/concurrency/deadlock")
        public Map<String, Object> deadlock() {
                Map<String, Object> body = new LinkedHashMap<>(deadlockDemo.describe());
                body.put("starvation", starvationDemo.describe());
                return body;
        }

        @GetMapping("/concurrency/completable-future")
        public Map<String, Object> completableFuture() {
                Map<String, String> previousContext = MDC.getCopyOfContextMap();
                if (previousContext == null || previousContext.get("correlationId") == null) {
                        previousContext = Map.of("correlationId", "learning-cf-demo");
                        MDC.setContextMap(previousContext);
                }
                CompletableFutureDemoResult result = completableFutureDemo.run(MDC.get("correlationId"));
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("topic", "CompletableFuture");
                body.put("structure", "CompletableFuture + ExecutorService");
                body.put("whyThisStructure", "CompletableFuture composes async stages cleanly, but production code must still supply a managed executor and propagate MDC manually.");
                body.put("result", result);
                body.put("tradeOffs", List.of(
                                "CompletableFuture is expressive for composition but can hide thread usage if the executor is implicit.",
                                "MDC does not flow automatically across async boundaries, so context propagation must be explicit."
                ));
                body.put("interviewTopics", List.of(
                                "Why avoid ForkJoinPool.commonPool() in backend services?",
                                "How do you propagate MDC to async stages?",
                                "When would you choose CompletableFuture over plain ExecutorService?"
                ));
                return body;
        }

        @GetMapping("/concurrency/thread-pool")
        public Map<String, Object> threadPool() {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("topic", "Thread pool sizing");
                body.put("structure", "Fixed ExecutorService");
                body.put("whyThisStructure", "The processing engine uses a named fixed pool so worker count is explicit, bounded and observable.");
                body.put("workerCount", executionProperties.workerCount());
                body.put("queueCapacity", executionProperties.queueCapacity());
                body.put("tradeOffs", List.of(
                                "A fixed pool prevents unbounded thread growth but can queue work under load.",
                                "Too many workers can increase contention and context switching."
                ));
                body.put("interviewTopics", List.of(
                                "How do CPU-bound and IO-bound workloads affect pool sizing?",
                                "Why is a bounded queue important for backpressure?",
                                "What production signals indicate thread-pool saturation?"
                ));
                return body;
        }
}
