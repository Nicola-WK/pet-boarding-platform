package com.petboarding.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PetRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 1024) String avatar,
        @NotBlank @Pattern(regexp = "dog|cat|other") String type,
        @Size(max = 80) String breed,
        @Min(0) @Max(2) Integer gender,
        LocalDateTime birthday,
        @DecimalMin("0.01") @Digits(integer = 4, fraction = 2) BigDecimal weight,
        @Size(max = 2000) String vaccine,
        @Size(max = 2000) String character,
        @Size(max = 2000) String diet,
        @Size(max = 2000) String remark) {
}
