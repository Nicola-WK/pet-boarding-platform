package com.petboarding.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public record ReviewRequest(
        @NotNull @Min(1) @Max(5) Integer score,
        @Size(max = 2000) String content,
        @Size(max = 10) List<@NotBlank @URL(protocol = "https") @Size(max = 1024) String> images) {
}
