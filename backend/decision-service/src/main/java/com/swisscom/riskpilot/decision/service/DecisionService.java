package com.swisscom.riskpilot.decision.service;

import com.swisscom.riskpilot.decision.domain.Decision;
import com.swisscom.riskpilot.decision.dto.DecisionRequest;
import com.swisscom.riskpilot.decision.dto.DecisionResponse;
import com.swisscom.riskpilot.decision.exception.ResourceNotFoundException;
import com.swisscom.riskpilot.decision.repository.DecisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DecisionService {

    private final DecisionRepository decisionRepository;
    private final AuditService auditService;

    @Transactional
    public DecisionResponse createDecision(DecisionRequest request, String reviewerUsername) {
        String reviewer = (reviewerUsername != null && !reviewerUsername.isBlank())
                ? reviewerUsername
                : "reviewer";

        Decision decision = Decision.builder()
                .riskId(request.riskId())
                .reviewer(reviewer)
                .decision(request.decision())
                .justification(request.justification())
                .evidenceReviewed(request.evidenceReviewed())
                .confidenceLevel(request.confidenceLevel())
                .build();

        Decision savedDecision = decisionRepository.save(decision);
        log.info("Decision created id={} for riskId={} by reviewer={}",
                savedDecision.getId(), savedDecision.getRiskId(), savedDecision.getReviewer());

        String auditDetails = String.format("Decision: %s, Confidence: %s, Justification: %s",
                request.decision(),
                request.confidenceLevel() != null ? request.confidenceLevel() : "N/A",
                request.justification());

        auditService.logEntry(
                request.riskId(),
                "DECISION_CREATED",
                reviewer,
                auditDetails
        );

        return DecisionResponse.fromEntity(savedDecision);
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> getDecisionsByRiskId(Long riskId) {
        return decisionRepository.findByRiskIdOrderByDecidedAtDesc(riskId)
                .stream()
                .map(DecisionResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DecisionResponse getDecisionById(Long id) {
        Decision decision = decisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with id: " + id));
        return DecisionResponse.fromEntity(decision);
    }
}
