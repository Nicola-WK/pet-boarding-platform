package com.petboarding.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(String openids) {
    public Set<String> allowedOpenids() {
        if (openids == null || openids.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(openids.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }
}