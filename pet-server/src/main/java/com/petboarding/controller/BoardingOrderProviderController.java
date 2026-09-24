package com.petboarding.controller;

import com.petboarding.security.CurrentUser;
import com.petboarding.service.BoardingOrderService;
import com.petboarding.vo.ApiResponse;
import com.petboarding.vo.BoardingOrderResponse;
import com.petboarding.vo.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/provider/orders")
public class BoardingOrderProviderController {
    private final BoardingOrderService service;

    public BoardingOrderProviderController(BoardingOrderService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResponse<BoardingOrderResponse>> list(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer status,
            @Min(1) @RequestParam(defaultValue = "1") int page,
            @Min(1) @Max(50) @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.providerOrders(CurrentUser.id(jwt), status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<BoardingOrderResponse> get(@AuthenticationPrincipal Jwt jwt,
            @Positive @PathVariable long id) {
        return ApiResponse.success(service.getProviderOrder(CurrentUser.id(jwt), id));
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<BoardingOrderResponse> confirm(@AuthenticationPrincipal Jwt jwt,
            @Positive @PathVariable long id) {
        return ApiResponse.success(service.confirm(CurrentUser.id(jwt), id));
    }

    @PostMapping("/{id}/start")
    public ApiResponse<BoardingOrderResponse> start(@AuthenticationPrincipal Jwt jwt,
            @Positive @PathVariable long id) {
        return ApiResponse.success(service.start(CurrentUser.id(jwt), id));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<BoardingOrderResponse> complete(@AuthenticationPrincipal Jwt jwt,
            @Positive @PathVariable long id) {
        return ApiResponse.success(service.complete(CurrentUser.id(jwt), id));
    }
}
