package com.swisscom.riskpilot.risk.dto;

import com.swisscom.riskpilot.risk.domain.RiskSeverity;
import com.swisscom.riskpilot.risk.domain.RiskStatus;

import java.time.LocalDateTime;

public record RiskResponse(
        Long id,
        String title,
        String description,
        String area,
        RiskSeverity severity,
        RiskStatus status,
        String owner,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy
) {}
