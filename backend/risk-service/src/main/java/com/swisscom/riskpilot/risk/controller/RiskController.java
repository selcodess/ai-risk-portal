package com.swisscom.riskpilot.risk.controller;

import com.swisscom.riskpilot.risk.dto.PagedResponse;
import com.swisscom.riskpilot.risk.dto.RiskRequest;
import com.swisscom.riskpilot.risk.dto.RiskResponse;
import com.swisscom.riskpilot.risk.service.RiskService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risks")
@RequiredArgsConstructor
public class RiskController {

    private final RiskService riskService;

    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER','REVIEWER','ADMIN')")
    public ResponseEntity<PagedResponse<RiskResponse>> listRisks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<RiskResponse> result = (status != null && !status.isBlank())
                ? riskService.findByStatus(status, pageable)
                : riskService.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER','REVIEWER','ADMIN')")
    public ResponseEntity<RiskResponse> getById(@PathVariable Long id) {
        return riskService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('REVIEWER','ADMIN')")
    public ResponseEntity<?> create(
            @Valid @RequestBody RiskRequest request,
            Authentication authentication
    ) {
        try {
            String createdBy = authentication.getName();
            RiskResponse created = riskService.create(request, createdBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('REVIEWER','ADMIN')")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody RiskRequest request
    ) {
        try {
            RiskResponse updated = riskService.update(id, request);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            riskService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
