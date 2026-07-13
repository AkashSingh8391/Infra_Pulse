package com.infrapulse.backend.dto.ai;

import com.infrapulse.backend.enums.Priority;

public record PriorityPredictionResponse(Priority priority, double confidence) {}
