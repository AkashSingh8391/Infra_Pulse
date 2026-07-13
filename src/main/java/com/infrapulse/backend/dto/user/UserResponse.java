package com.infrapulse.backend.dto.user;

import com.infrapulse.backend.enums.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        String avatarUrl,
        Role role,
        Long departmentId,
        String departmentName,
        boolean active
) {}
