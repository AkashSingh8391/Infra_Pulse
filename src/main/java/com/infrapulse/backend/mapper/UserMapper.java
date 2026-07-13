package com.infrapulse.backend.mapper;

import com.infrapulse.backend.dto.user.UserResponse;
import com.infrapulse.backend.entity.User;

public class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                user.isActive()
        );
    }
}
