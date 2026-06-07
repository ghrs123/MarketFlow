package com.gustavo.marketflow.learning;

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
 * Read-only endpoints that expose the concepts introduced in Phase 1.
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

    public LearningController(ApplicationContext applicationContext,
                              @Value("${spring.profiles.active:default}") String activeProfile) {
        this.applicationContext = applicationContext;
        this.activeProfile = activeProfile;
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
        body.put("endpointsInPhase1", List.of(
                "POST /orders",
                "GET  /orders/{id}",
                "GET  /orders"
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
}
