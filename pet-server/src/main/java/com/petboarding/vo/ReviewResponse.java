package com.petboarding.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import com.petboarding.entity.Review;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(@JsonSerialize(using = ToStringSerializer.class) Long id, @JsonSerialize(using = ToStringSerializer.class) Long providerId, @JsonSerialize(using = ToStringSerializer.class) Long serviceId, Integer score,
                             String content, List<String> images, LocalDateTime createdAt) {
    public static ReviewResponse from(Review review, List<String> images) {
        return new ReviewResponse(review.getId(), review.getProviderId(), review.getServiceId(),
                review.getScore(), review.getContent(), images, review.getCreatedAt());
    }
}
