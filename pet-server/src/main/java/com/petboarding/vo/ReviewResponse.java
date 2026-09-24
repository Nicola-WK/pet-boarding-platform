package com.petboarding.vo;

import com.petboarding.entity.Review;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(Long id, Long providerId, Long serviceId, Integer score,
                             String content, List<String> images, LocalDateTime createdAt) {
    public static ReviewResponse from(Review review, List<String> images) {
        return new ReviewResponse(review.getId(), review.getProviderId(), review.getServiceId(),
                review.getScore(), review.getContent(), images, review.getCreatedAt());
    }
}
