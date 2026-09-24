package com.petboarding.vo;

import com.petboarding.entity.Provider;

import java.math.BigDecimal;
import java.util.List;

public record ProviderPublicResponse(Long id, String name, String contactPhone, String address,
                                     BigDecimal latitude, BigDecimal longitude,
                                     String description, List<String> images) {
    public static ProviderPublicResponse from(Provider provider, List<String> images) {
        return new ProviderPublicResponse(provider.getId(), provider.getName(),
                provider.getContactPhone(), provider.getAddress(), provider.getLatitude(),
                provider.getLongitude(), provider.getDescription(), images);
    }
}