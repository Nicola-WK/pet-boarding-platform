package com.petboarding.controller;

import com.petboarding.service.ProviderService;
import com.petboarding.vo.ApiResponse;
import com.petboarding.vo.PageResponse;
import com.petboarding.vo.ProviderPublicResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/providers")
public class ProviderController {
    private final ProviderService service;

    public ProviderController(ProviderService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResponse<ProviderPublicResponse>> list(
            @Size(max = 80) @RequestParam(required = false) String keyword,
            @Min(1) @RequestParam(defaultValue = "1") int page,
            @Min(1) @Max(50) @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.listApproved(keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProviderPublicResponse> get(@Positive @PathVariable long id) {
        return ApiResponse.success(service.getApproved(id));
    }
}