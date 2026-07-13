package com.infrapulse.backend.dto.complaint;

public record ComplaintStatsResponse(
        long total,
        long pending,
        long inProgress,
        long resolved
) {}
