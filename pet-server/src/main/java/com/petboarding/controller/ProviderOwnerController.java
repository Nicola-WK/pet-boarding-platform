package com.petboarding.controller;

import com.petboarding.dto.ProviderApplicationRequest;
import com.petboarding.security.CurrentUser;
import com.petboarding.service.ProviderService;
import com.petboarding.vo.ApiResponse;
import com.petboarding.vo.ProviderPrivateResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/provider")
public class ProviderOwnerController {
    private final ProviderService service;

    public ProviderOwnerController(ProviderService service) {
        this.service = service;
    }

    @PostMapping("/apply")
    public ApiResponse<ProviderPrivateResponse> apply(@AuthenticationPrincipal Jwt jwt,
                                                      @Valid @RequestBody ProviderApplicationRequest request) {
        return ApiResponse.success(service.apply(CurrentUser.id(jwt), request));
    }

    @GetMapping("/me")
    public ApiResponse<ProviderPrivateResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.mine(CurrentUser.id(jwt)));
    }

    @PutMapping("/me")
    public ApiResponse<ProviderPrivateResponse> update(@AuthenticationPrincipal Jwt jwt,
                                                       @Valid @RequestBody ProviderApplicationRequest request) {
        return ApiResponse.success(service.updateMine(CurrentUser.id(jwt), request));
    }
}