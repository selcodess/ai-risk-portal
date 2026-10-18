package com.swisscom.riskpilot.decision.service;

import com.swisscom.riskpilot.decision.domain.AuditEntry;
import com.swisscom.riskpilot.decision.dto.AuditResponse;
import com.swisscom.riskpilot.decision.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    @Transactional
    public AuditEntry logEntry(Long riskId, String action, String actor, String details) {
        AuditEntry entry = AuditEntry.builder()
                .riskId(riskId)
                .action(action)
                .actor(actor != null && !actor.isBlank() ? actor : "system")
                .details(details)
                .build();
        AuditEntry saved = auditRepository.save(entry);
        log.info("Audit entry created: action={}, riskId={}, actor={}", action, riskId, entry.getActor());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> getAuditEntriesByRiskId(Long riskId) {
        return auditRepository.findByRiskIdOrderByTimestampDesc(riskId)
                .stream()
                .map(AuditResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AuditResponse> getAllAuditEntries(Pageable pageable) {
        return auditRepository.findAllByOrderByTimestampDesc(pageable)
                .map(AuditResponse::fromEntity);
    }
}
