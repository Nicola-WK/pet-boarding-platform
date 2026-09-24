package com.petboarding.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import com.petboarding.entity.Provider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProviderPrivateResponse(@JsonSerialize(using = ToStringSerializer.class) Long id, @JsonSerialize(using = ToStringSerializer.class) Long ownerUserId, String name, String contactPhone,
                                      String address, BigDecimal latitude, BigDecimal longitude,
                                      String description, List<String> images, String licenseImage,
                                      Integer auditStatus, String auditReason, LocalDateTime auditedAt,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static ProviderPrivateResponse from(Provider provider, List<String> images) {
        return new ProviderPrivateResponse(provider.getId(), provider.getOwnerUserId(),
                provider.getName(), provider.getContactPhone(), provider.getAddress(),
                provider.getLatitude(), provider.getLongitude(), provider.getDescription(),
                images, provider.getLicenseImage(), provider.getAuditStatus(),
                provider.getAuditReason(), provider.getAuditedAt(), provider.getCreatedAt(),
                provider.getUpdatedAt());
    }
}