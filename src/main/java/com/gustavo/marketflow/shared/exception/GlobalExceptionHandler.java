package com.gustavo.marketflow.shared.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralised translation of exceptions into HTTP responses.
 *
 * <p>All error responses follow RFC 7807 via {@link ProblemDetail}, which
 * Spring 6 supports natively. This gives the API a single, well-known
 * error contract for clients ({@code type}, {@code title}, {@code status},
 * {@code detail}, {@code instance} plus extensions) and avoids leaking
 * internal exception messages or stack traces.</p>
 *
 * <p>Handled cases:
 * <ul>
 *   <li>{@link OrderNotFoundException} -&gt; 404 Not Found.</li>
 *   <li>{@link MethodArgumentNotValidException} (Bean Validation failure)
 *       -&gt; 400 Bad Request with the field-level error list.</li>
 *   <li>{@link HttpMessageNotReadableException} (malformed JSON, bad enum)
 *       -&gt; 400 Bad Request.</li>
 *   <li>{@link IllegalArgumentException} -&gt; 400 Bad Request.</li>
 *   <li>Any other unexpected {@link Exception} -&gt; 500 Internal Server
 *       Error. The exception is logged with a stack trace server-side but
 *       its message is not exposed to the client.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handleOrderNotFound(OrderNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Order not found");
        problem.setType(URI.create("https://marketflow.local/errors/order-not-found"));
        problem.setProperty("orderId", ex.getOrderId().toString());
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> {
                    Map<String, String> err = new LinkedHashMap<>();
                    err.put("field", fe.getField());
                    err.put("message", fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage());
                    return err;
                })
                .toList();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed");
        problem.setTitle("Validation error");
        problem.setType(URI.create("https://marketflow.local/errors/validation"));
        problem.setProperty("errors", errors);
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Malformed request body");
        problem.setTitle("Bad request");
        problem.setType(URI.create("https://marketflow.local/errors/malformed-request"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage());
        problem.setTitle("Invalid argument");
        problem.setType(URI.create("https://marketflow.local/errors/invalid-argument"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid request parameter: " + ex.getName());
        problem.setTitle("Invalid argument");
        problem.setType(URI.create("https://marketflow.local/errors/invalid-argument"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLocking(ObjectOptimisticLockingFailureException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "The resource was modified concurrently. Refresh and retry.");
        problem.setTitle("Optimistic locking conflict");
        problem.setType(URI.create("https://marketflow.local/errors/optimistic-locking"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        // Log full detail server-side, expose a generic message client-side.
        log.error("Unhandled exception", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error");
        problem.setTitle("Internal server error");
        problem.setType(URI.create("https://marketflow.local/errors/internal"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }
}
