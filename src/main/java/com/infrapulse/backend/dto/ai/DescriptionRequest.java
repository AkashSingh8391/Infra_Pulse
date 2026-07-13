package com.infrapulse.backend.dto.ai;

import jakarta.validation.constraints.NotBlank;

public record DescriptionRequest(@NotBlank String description) {}
