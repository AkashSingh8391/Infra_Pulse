package com.infrapulse.backend.dto.complaint;

import com.infrapulse.backend.enums.ComplaintCategory;
import com.infrapulse.backend.enums.ComplaintStatus;
import com.infrapulse.backend.enums.Priority;

import java.time.LocalDateTime;
import java.util.List;

public record ComplaintDetailResponse(
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
        Long citizenId,
        String citizenName,
        Long officerId,
        String officerName,
        boolean isBookmarked,
        Integer myRating,
        List<StatusHistoryResponse> statusHistory,
        List<CommentResponse> comments,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {}
