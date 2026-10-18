package com.swisscom.riskpilot.risk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RiskRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @NotBlank @Size(max = 100) String area,
        @NotBlank String severity,
        @NotBlank @Size(max = 100) String owner
) {}
