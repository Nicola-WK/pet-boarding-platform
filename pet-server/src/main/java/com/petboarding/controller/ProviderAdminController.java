package com.petboarding.controller;

import com.petboarding.dto.ProviderAuditRequest;
import com.petboarding.security.CurrentUser;
import com.petboarding.service.ProviderService;
import com.petboarding.vo.ApiResponse;
import com.petboarding.vo.PageResponse;
import com.petboarding.vo.ProviderPrivateResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/admin/providers")
public class ProviderAdminController {
    private final ProviderService service;

    public ProviderAdminController(ProviderService service) {
        this.service = service;
    }

    @GetMapping("/pending")
    public ApiResponse<PageResponse<ProviderPrivateResponse>> pending(
            @AuthenticationPrincipal Jwt jwt,
            @Min(1) @RequestParam(defaultValue = "1") int page,
            @Min(1) @Max(50) @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.pending(CurrentUser.id(jwt), page, size));
    }

    @PostMapping("/{id}/audit")
    public ApiResponse<ProviderPrivateResponse> audit(
            @AuthenticationPrincipal Jwt jwt,
            @Positive @PathVariable long id,
            @Valid @RequestBody ProviderAuditRequest request) {
        return ApiResponse.success(service.audit(CurrentUser.id(jwt), id, request));
    }
}