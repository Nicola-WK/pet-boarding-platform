package com.petboarding.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;
import java.util.Set;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secretBase64, String issuer, Duration ttl) {
    public JwtProperties {
        URI uri;
        try {
            uri = URI.create(issuer);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("JWT_ISSUER must be a valid URL", ex);
        }
        if (uri.getScheme() == null || !Set.of("http", "https").contains(uri.getScheme()) || uri.getHost() == null) {
            throw new IllegalArgumentException("JWT_ISSUER must be an HTTP(S) URL");
        }
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("JWT_TTL must be positive");
        }
    }
}
