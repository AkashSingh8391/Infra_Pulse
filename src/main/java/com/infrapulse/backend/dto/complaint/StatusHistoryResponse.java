package com.infrapulse.backend.dto.complaint;

import com.infrapulse.backend.enums.ComplaintStatus;

import java.time.LocalDateTime;

public record StatusHistoryResponse(
        ComplaintStatus status,
        String note,
        String photoUrl,
        LocalDateTime timestamp
) {}
