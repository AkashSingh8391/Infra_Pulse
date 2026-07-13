package com.infrapulse.backend.dto.complaint;

import java.util.List;

public record PagedComplaintResponse(
        List<ComplaintSummaryResponse> content,
        int totalPages,
        long totalElements,
        ComplaintStatsResponse stats
) {}
