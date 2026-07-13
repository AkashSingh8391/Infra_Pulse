package com.infrapulse.backend.dto.complaint;

import java.util.List;

public record AnalyticsResponse(
        AnalyticsStats stats,
        List<TrendPoint> trend,
        List<TrendPoint> monthlyTrend,
        List<CategoryBreakdown> byCategory
) {}
