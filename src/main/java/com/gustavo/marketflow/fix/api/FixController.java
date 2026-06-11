package com.gustavo.marketflow.fix.api;

import java.net.URI;
import java.util.UUID;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.gustavo.marketflow.fix.application.FixMessageApplicationService;
import com.gustavo.marketflow.fix.domain.FixMessage;

/**
 * REST adapter for generating, retrieving and explaining simulated FIX messages.
 */
@RestController
@RequestMapping
@PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class FixController {

    private final FixMessageApplicationService fixMessageApplicationService;

    public FixController(FixMessageApplicationService fixMessageApplicationService) {
        this.fixMessageApplicationService = fixMessageApplicationService;
    }

    @PostMapping("/orders/{id}/fix-message")
    public ResponseEntity<FixMessageResponse> generate(@PathVariable UUID id) {
        FixMessage generated = fixMessageApplicationService.generateForOrder(id);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();
        return ResponseEntity.created(location).body(FixMessageResponse.from(generated));
    }

    @GetMapping("/orders/{id}/fix-message")
    public FixMessageResponse findByOrderId(@PathVariable UUID id) {
        return FixMessageResponse.from(fixMessageApplicationService.findByOrderId(id));
    }

    @GetMapping("/orders/{id}/fix-explanation")
    public FixExplanationResponse explainByOrderId(@PathVariable UUID id) {
        return FixExplanationResponse.from(id, fixMessageApplicationService.explainByOrderId(id));
    }

    @PostMapping("/fix/explain")
    public FixExplanationResponse explainRaw(@Valid @RequestBody RawFixMessageRequest request) {
        return FixExplanationResponse.from(null, fixMessageApplicationService.explainRaw(request.rawMessage()));
    }
}
