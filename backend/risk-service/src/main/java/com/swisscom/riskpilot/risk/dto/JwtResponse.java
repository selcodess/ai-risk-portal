package com.swisscom.riskpilot.risk.dto;

public record JwtResponse(
        String token,
        String username,
        String role,
        long expiresIn
) {}
