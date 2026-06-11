package com.gustavo.marketflow.fix.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gustavo.marketflow.event.domain.FixMessageGeneratedEvent;
import com.gustavo.marketflow.event.infrastructure.InMemoryEventBus;
import com.gustavo.marketflow.fix.domain.FixMessage;
import com.gustavo.marketflow.fix.domain.FixMessageRepository;
import com.gustavo.marketflow.fix.domain.FixTagExplanation;
import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderRepository;
import com.gustavo.marketflow.shared.exception.FixMessageAlreadyExistsException;
import com.gustavo.marketflow.shared.exception.FixMessageNotFoundException;
import com.gustavo.marketflow.shared.exception.OrderNotFoundException;

/**
 * Orchestrates generation, persistence, retrieval and explanation of simulated FIX messages.
 */
@Service
public class FixMessageApplicationService {

    private static final Logger log = LoggerFactory.getLogger(FixMessageApplicationService.class);

    private final OrderRepository orderRepository;
    private final FixMessageRepository fixMessageRepository;
    private final FixMessageGenerator fixMessageGenerator;
    private final FixMessageExplainer fixMessageExplainer;
    private final InMemoryEventBus eventBus;
    private final Clock clock;
    private final Counter generationSuccessCounter;
    private final Counter generationFailureCounter;
    private final Counter explanationSuccessCounter;
    private final Counter explanationFailureCounter;
    private final Timer generationTimer;

    public FixMessageApplicationService(OrderRepository orderRepository,
                                        FixMessageRepository fixMessageRepository,
                                        FixMessageGenerator fixMessageGenerator,
                                        FixMessageExplainer fixMessageExplainer,
                                        InMemoryEventBus eventBus,
                                        Clock clock,
                                        MeterRegistry meterRegistry) {
        this.orderRepository = orderRepository;
        this.fixMessageRepository = fixMessageRepository;
        this.fixMessageGenerator = fixMessageGenerator;
        this.fixMessageExplainer = fixMessageExplainer;
        this.eventBus = eventBus;
        this.clock = clock;
        this.generationSuccessCounter = meterRegistry.counter("marketflow.fix.messages.generated");
        this.generationFailureCounter = meterRegistry.counter("marketflow.fix.messages.failed");
        this.explanationSuccessCounter = meterRegistry.counter("marketflow.fix.explanations.completed");
        this.explanationFailureCounter = meterRegistry.counter("marketflow.fix.explanations.failed");
        this.generationTimer = meterRegistry.timer("marketflow.fix.generation.duration");
    }

    @Transactional
    public FixMessage generateForOrder(UUID orderId) {
        return generationTimer.record(() -> generateAndPersist(orderId));
    }

    @Transactional(readOnly = true)
    public FixMessage findByOrderId(UUID orderId) {
        requireOrder(orderId);
        return fixMessageRepository.findByOrderId(orderId)
                .orElseThrow(() -> new FixMessageNotFoundException(orderId));
    }

    @Transactional(readOnly = true)
    public List<FixTagExplanation> explainByOrderId(UUID orderId) {
        return explain(findByOrderId(orderId).rawMessage());
    }

    public List<FixTagExplanation> explainRaw(String rawMessage) {
        return explain(rawMessage);
    }

    private FixMessage generateAndPersist(UUID orderId) {
        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        try {
            Order order = requireOrder(orderId);
            if (fixMessageRepository.existsByOrderId(orderId)) {
                throw new FixMessageAlreadyExistsException(orderId);
            }

            Instant now = clock.instant();
            FixMessage saved = fixMessageRepository.save(new FixMessage(
                    UUID.randomUUID(),
                    orderId,
                    fixMessageGenerator.generate(order),
                    now,
                    now
            ));
            generationSuccessCounter.increment();
            eventBus.publish(FixMessageGeneratedEvent.now(orderId));
            MDC.put("orderId", orderId.toString());
            log.info("Simulated FIX message generated for order {}", orderId);
            return saved;
        } catch (RuntimeException ex) {
            generationFailureCounter.increment();
            throw ex;
        } finally {
            restoreMdc(previousContext);
        }
    }

    private Order requireOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private List<FixTagExplanation> explain(String rawMessage) {
        try {
            List<FixTagExplanation> explanation = fixMessageExplainer.explain(rawMessage);
            explanationSuccessCounter.increment();
            return explanation;
        } catch (RuntimeException ex) {
            explanationFailureCounter.increment();
            throw ex;
        }
    }

    private void restoreMdc(Map<String, String> contextMap) {
        if (contextMap == null || contextMap.isEmpty()) {
            MDC.clear();
            return;
        }
        MDC.setContextMap(contextMap);
    }
}
