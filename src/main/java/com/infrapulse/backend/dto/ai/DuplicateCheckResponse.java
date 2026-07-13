package com.infrapulse.backend.dto.ai;

public record DuplicateCheckResponse(boolean isDuplicate, int matchCount, Long closestComplaintId) {}
