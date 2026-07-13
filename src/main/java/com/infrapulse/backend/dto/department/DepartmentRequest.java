package com.infrapulse.backend.dto.department;

import com.infrapulse.backend.enums.ComplaintCategory;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record DepartmentRequest(
        @NotBlank String name,
        String description,
        Set<ComplaintCategory> handledCategories
) {}
