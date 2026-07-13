package com.infrapulse.backend.dto.complaint;

import jakarta.validation.constraints.NotNull;

public record AssignRequest(@NotNull Long officerId) {}
