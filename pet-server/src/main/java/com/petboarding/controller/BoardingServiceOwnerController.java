package com.petboarding.controller;

import com.petboarding.dto.BoardingServiceRequest;
import com.petboarding.dto.ServiceStatusRequest;
import com.petboarding.security.CurrentUser;
import com.petboarding.service.BoardingServiceService;
import com.petboarding.vo.ApiResponse;
import com.petboarding.vo.BoardingServiceResponse;
import com.petboarding.vo.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/provider/services")
public class BoardingServiceOwnerController {
    private final BoardingServiceService service;

    public BoardingServiceOwnerController(BoardingServiceService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<BoardingServiceResponse> create(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BoardingServiceRequest request) {
        return ApiResponse.success(service.create(CurrentUser.id(jwt), request));
    }

    @GetMapping
    public ApiResponse<PageResponse<BoardingServiceResponse>> mine(@AuthenticationPrincipal Jwt jwt,
            @Min(1) @RequestParam(defaultValue = "1") int page,
            @Min(1) @Max(50) @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.mine(CurrentUser.id(jwt), page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<BoardingServiceResponse> update(@AuthenticationPrincipal Jwt jwt,
            @Positive @PathVariable long id, @Valid @RequestBody BoardingServiceRequest request) {
        return ApiResponse.success(service.update(CurrentUser.id(jwt), id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<BoardingServiceResponse> status(@AuthenticationPrincipal Jwt jwt,
            @Positive @PathVariable long id, @Valid @RequestBody ServiceStatusRequest request) {
        return ApiResponse.success(service.setActive(CurrentUser.id(jwt), id, request.active()));
    }
}
