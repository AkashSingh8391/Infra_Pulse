package com.infrapulse.backend.dto.complaint;

import jakarta.validation.constraints.NotBlank;

public record RejectRequest(@NotBlank String reason) {}
