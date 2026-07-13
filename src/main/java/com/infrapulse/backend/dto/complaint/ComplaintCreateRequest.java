package com.infrapulse.backend.dto.complaint;

import com.infrapulse.backend.enums.ComplaintCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComplaintCreateRequest(
        @NotBlank @Size(min = 15, message = "Description must be at least 15 characters") String description,
        @NotBlank String title,
        @NotNull ComplaintCategory category,
        @NotNull Double latitude,
        @NotNull Double longitude,
        String address,
        String imageUrl
) {}
