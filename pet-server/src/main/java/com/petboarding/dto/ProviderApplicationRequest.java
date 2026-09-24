package com.petboarding.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProviderApplicationRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(min = 8, max = 32)
        @Pattern(regexp = "[+0-9() -]+") String contactPhone,
        @NotBlank @Size(max = 255) String address,
        @DecimalMin("-90.0") @DecimalMax("90.0")
        @Digits(integer = 3, fraction = 7) BigDecimal latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0")
        @Digits(integer = 3, fraction = 7) BigDecimal longitude,
        @Size(max = 5000) String description,
        @Size(max = 10) List<@NotBlank @URL(protocol = "https") @Size(max = 1024) String> images,
        @URL(protocol = "https") @Size(max = 1024) String licenseImage) {
}