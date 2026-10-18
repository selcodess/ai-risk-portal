package com.swisscom.riskpilot.decision.controller;

import com.swisscom.riskpilot.decision.dto.DecisionRequest;
import com.swisscom.riskpilot.decision.dto.DecisionResponse;
import com.swisscom.riskpilot.decision.service.DecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Decisions", description = "AI Risk Decision management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/decisions")
@RequiredArgsConstructor
public class DecisionController {

    private final DecisionService decisionService;

    @Operation(summary = "Submit a decision for an AI risk")
    @PostMapping
    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public ResponseEntity<DecisionResponse> createDecision(
            @Valid @RequestBody DecisionRequest request,
            Authentication authentication
    ) {
        String reviewer = (authentication != null && authentication.getName() != null)
                ? authentication.getName()
                : "reviewer";
        DecisionResponse response = decisionService.createDecision(request, reviewer);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all decisions for a specific risk ID")
    @GetMapping("/risk/{riskId}")
    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public ResponseEntity<List<DecisionResponse>> getDecisionsByRiskId(@PathVariable Long riskId) {
        List<DecisionResponse> responses = decisionService.getDecisionsByRiskId(riskId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get a single decision by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public ResponseEntity<DecisionResponse> getDecisionById(@PathVariable Long id) {
        DecisionResponse response = decisionService.getDecisionById(id);
        return ResponseEntity.ok(response);
    }
}
