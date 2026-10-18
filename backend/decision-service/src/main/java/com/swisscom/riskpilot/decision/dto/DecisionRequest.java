package com.swisscom.riskpilot.decision.dto;

import com.swisscom.riskpilot.decision.domain.DecisionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DecisionRequest(
        @NotNull(message = "Risk ID is required")
        Long riskId,

        @NotNull(message = "Decision status is required")
        DecisionStatus decision,

        @NotBlank(message = "Justification is required")
        @Size(min = 20, max = 2000, message = "Justification must be between 20 and 2000 characters")
        String justification,

        String evidenceReviewed,

        String confidenceLevel
) {}
