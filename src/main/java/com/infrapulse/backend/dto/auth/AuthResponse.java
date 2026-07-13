package com.infrapulse.backend.dto.auth;

import com.infrapulse.backend.dto.user.UserResponse;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {}
