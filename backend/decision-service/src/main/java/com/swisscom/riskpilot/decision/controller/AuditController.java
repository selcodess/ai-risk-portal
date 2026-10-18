package com.swisscom.riskpilot.decision.controller;

import com.swisscom.riskpilot.decision.dto.AuditResponse;
import com.swisscom.riskpilot.decision.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Audit", description = "Audit trail endpoints for decisions and risk lifecycle")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "List all audit entries with pagination")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    public ResponseEntity<Page<AuditResponse>> getAllAuditEntries(
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AuditResponse> entries = auditService.getAllAuditEntries(pageable);
        return ResponseEntity.ok(entries);
    }

    @Operation(summary = "List audit entries for a specific risk ID")
    @GetMapping("/risk/{riskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    public ResponseEntity<List<AuditResponse>> getAuditEntriesByRiskId(@PathVariable Long riskId) {
        List<AuditResponse> entries = auditService.getAuditEntriesByRiskId(riskId);
        return ResponseEntity.ok(entries);
    }
}
