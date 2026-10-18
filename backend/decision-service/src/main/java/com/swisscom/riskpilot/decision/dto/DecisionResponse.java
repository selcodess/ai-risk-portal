package com.swisscom.riskpilot.decision.dto;

import com.swisscom.riskpilot.decision.domain.Decision;
import com.swisscom.riskpilot.decision.domain.DecisionStatus;

import java.time.LocalDateTime;

public record DecisionResponse(
        Long id,
        Long riskId,
        String reviewer,
        DecisionStatus decision,
        String justification,
        String evidenceReviewed,
        String confidenceLevel,
        LocalDateTime decidedAt
) {
    public static DecisionResponse fromEntity(Decision decision) {
        return new DecisionResponse(
                decision.getId(),
                decision.getRiskId(),
                decision.getReviewer(),
                decision.getDecision(),
                decision.getJustification(),
                decision.getEvidenceReviewed(),
                decision.getConfidenceLevel(),
                decision.getDecidedAt()
        );
    }
}
