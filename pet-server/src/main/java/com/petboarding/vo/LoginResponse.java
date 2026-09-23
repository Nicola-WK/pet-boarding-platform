package com.petboarding.vo;

public record LoginResponse(String token, String tokenType, long expiresIn, UserProfile user) {
}
