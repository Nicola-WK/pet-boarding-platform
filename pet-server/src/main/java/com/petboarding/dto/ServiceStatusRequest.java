package com.petboarding.dto;

import jakarta.validation.constraints.NotNull;

public record ServiceStatusRequest(@NotNull Boolean active) {
}
