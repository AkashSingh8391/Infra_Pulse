package com.infrapulse.backend.dto.complaint;

public record AnalyticsStats(
        Long officers,
        Long activeComplaints,
        Long resolvedThisMonth,
        Double avgResolutionDays,
        Long totalUsers,
        Long departments,
        Long totalComplaints,
        Long criticalOpen,
        Double satisfactionScore,
        Double reopenRate,
        Double slaCompliance
) {}
