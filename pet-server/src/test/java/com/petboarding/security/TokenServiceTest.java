package com.petboarding.security;

import com.petboarding.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenServiceTest {
    private final SecurityConfig config = new SecurityConfig();

    @Test
    void issuedTokenHasVerifiedSubjectAndIssuer() {
        JwtProperties properties = properties((byte) 7);
        var key = config.jwtSecretKey(properties);
        TokenService service = new TokenService(config.jwtEncoder(key), properties);

        Jwt jwt = config.jwtDecoder(key, properties).decode(service.issue(42L));

        assertEquals("42", jwt.getSubject());
        assertEquals("https://pet.example.test", jwt.getIssuer().toString());
        assertEquals(3600, service.expiresInSeconds());
    }

    @Test
    void tokenSignedByAnotherKeyIsRejected() {
        JwtProperties properties = properties((byte) 7);
        TokenService service = new TokenService(
                config.jwtEncoder(config.jwtSecretKey(properties)), properties);
        String token = service.issue(42L);
        var wrongKey = config.jwtSecretKey(properties((byte) 8));

        assertThrows(JwtException.class, () -> config.jwtDecoder(wrongKey, properties).decode(token));
    }

    private JwtProperties properties(byte value) {
        byte[] secret = new byte[32];
        java.util.Arrays.fill(secret, value);
        return new JwtProperties(Base64.getEncoder().encodeToString(secret),
                "https://pet.example.test", Duration.ofHours(1));
    }
}
