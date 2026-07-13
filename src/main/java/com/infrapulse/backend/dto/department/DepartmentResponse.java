package com.infrapulse.backend.dto.department;

public record DepartmentResponse(
        Long id,
        String name,
        String description,
        long officerCount,
        long activeComplaintCount
) {}
