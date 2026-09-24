package com.petboarding.controller;

import com.petboarding.dto.ReviewRequest;
import com.petboarding.security.CurrentUser;
import com.petboarding.service.ReviewService;
import com.petboarding.vo.ApiResponse;
import com.petboarding.vo.PageResponse;
import com.petboarding.vo.ReviewResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class ReviewController {
    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @PostMapping("/orders/{orderId}/review")
    public ApiResponse<ReviewResponse> create(@AuthenticationPrincipal Jwt jwt,
            @Positive @PathVariable long orderId, @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.success(service.create(CurrentUser.id(jwt), orderId, request));
    }

    @GetMapping("/orders/{orderId}/review")
    public ApiResponse<ReviewResponse> mine(@AuthenticationPrincipal Jwt jwt,
            @Positive @PathVariable long orderId) {
        return ApiResponse.success(service.getMine(CurrentUser.id(jwt), orderId));
    }

    @GetMapping("/providers/{id}/reviews")
    public ApiResponse<PageResponse<ReviewResponse>> byProvider(@Positive @PathVariable long id,
            @Min(1) @RequestParam(defaultValue = "1") int page,
            @Min(1) @Max(50) @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.listByProvider(id, page, size));
    }

    @GetMapping("/services/{id}/reviews")
    public ApiResponse<PageResponse<ReviewResponse>> byService(@Positive @PathVariable long id,
            @Min(1) @RequestParam(defaultValue = "1") int page,
            @Min(1) @Max(50) @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.listByService(id, page, size));
    }
}
