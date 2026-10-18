package com.swisscom.riskpilot.decision.dto;

import com.swisscom.riskpilot.decision.domain.AuditEntry;

import java.time.LocalDateTime;

public record AuditResponse(
        Long id,
        Long riskId,
        String action,
        String actor,
        String details,
        LocalDateTime timestamp
) {
    public static AuditResponse fromEntity(AuditEntry auditEntry) {
        return new AuditResponse(
                auditEntry.getId(),
                auditEntry.getRiskId(),
                auditEntry.getAction(),
                auditEntry.getActor(),
                auditEntry.getDetails(),
                auditEntry.getTimestamp()
        );
    }
}
