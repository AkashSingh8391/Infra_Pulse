package com.infrapulse.backend.dto.user;

public record OfficerResponse(
        Long id,
        String name,
        String email,
        long activeCount,
        long resolvedCount,
        Double rating,
        Double avgResolutionDays
) {}
