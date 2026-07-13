package com.infrapulse.backend.dto.complaint;

import com.infrapulse.backend.enums.ComplaintCategory;
import com.infrapulse.backend.enums.ComplaintStatus;
import com.infrapulse.backend.enums.Priority;

import java.time.LocalDateTime;

public record ComplaintSummaryResponse(
        Long id,
        String title,
        String description,
        ComplaintCategory category,
        ComplaintStatus status,
        Priority priority,
        Double latitude,
        Double longitude,
        String address,
        String imageUrl,
        int commentCount,
        Integer rating,
        boolean isBookmarked,
        LocalDateTime createdAt
) {}
