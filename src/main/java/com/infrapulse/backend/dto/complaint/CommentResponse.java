package com.infrapulse.backend.dto.complaint;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String authorName,
        Long authorId,
        String message,
        LocalDateTime createdAt
) {}
