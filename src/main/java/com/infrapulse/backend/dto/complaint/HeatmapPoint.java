package com.infrapulse.backend.dto.complaint;

public record HeatmapPoint(
        Long id,
        String title,
        String address,
        com.infrapulse.backend.enums.ComplaintStatus status,
        com.infrapulse.backend.enums.ComplaintCategory category,
        Double latitude,
        Double longitude
) {}
