package com.petboarding.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProviderAuditRequest(@NotNull Boolean approved,
                                   @Size(max = 500) String reason) {
}