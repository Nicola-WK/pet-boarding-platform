package com.petboarding.controller;

import com.petboarding.dto.CreateOrderRequest;
import com.petboarding.security.CurrentUser;
import com.petboarding.service.BoardingOrderService;
import com.petboarding.vo.ApiResponse;
import com.petboarding.vo.BoardingOrderResponse;
import com.petboarding.vo.PageResponse;
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
@RequestMapping("/orders")
public class BoardingOrderController {
    private final BoardingOrderService service;

    public BoardingOrderController(BoardingOrderService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<BoardingOrderResponse> create(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(service.create(CurrentUser.id(jwt), request));
    }

    @GetMapping
    public ApiResponse<PageResponse<BoardingOrderResponse>> mine(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer status,
            @Min(1) @RequestParam(defaultValue = "1") int page,
            @Min(1) @Max(50) @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.mine(CurrentUser.id(jwt), status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<BoardingOrderResponse> get(@AuthenticationPrincipal Jwt jwt,
            @Positive @PathVariable long id) {
        return ApiResponse.success(service.getMine(CurrentUser.id(jwt), id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<BoardingOrderResponse> cancel(@AuthenticationPrincipal Jwt jwt,
            @Positive @PathVariable long id) {
        return ApiResponse.success(service.cancel(CurrentUser.id(jwt), id));
    }
}
