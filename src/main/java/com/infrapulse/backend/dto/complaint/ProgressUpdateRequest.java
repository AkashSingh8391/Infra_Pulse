package com.infrapulse.backend.dto.complaint;

import com.infrapulse.backend.enums.ComplaintStatus;
import jakarta.validation.constraints.NotNull;

public record ProgressUpdateRequest(
        @NotNull ComplaintStatus status,
        String note,
        String photoUrl
) {}
