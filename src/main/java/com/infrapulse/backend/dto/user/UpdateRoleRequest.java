package com.infrapulse.backend.dto.user;

import com.infrapulse.backend.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(@NotNull Role role) {}
