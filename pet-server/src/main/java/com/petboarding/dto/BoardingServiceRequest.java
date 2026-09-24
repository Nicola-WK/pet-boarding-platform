package com.petboarding.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.List;

public record BoardingServiceRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @NotBlank @Pattern(regexp = "dog|cat|other") String petType,
        @NotNull @Min(1) Integer capacity,
        @Size(max = 5000) String description,
        @Size(max = 10) List<@NotBlank @URL(protocol = "https") @Size(max = 1024) String> images) {
}
