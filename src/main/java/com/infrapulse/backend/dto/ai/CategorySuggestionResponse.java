package com.infrapulse.backend.dto.ai;

import com.infrapulse.backend.enums.ComplaintCategory;

public record CategorySuggestionResponse(ComplaintCategory category, double confidence) {}
