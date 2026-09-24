package com.petboarding.controller;

import com.petboarding.security.CurrentUser;
import com.petboarding.service.DemoPaymentService;
import com.petboarding.vo.ApiResponse;
import com.petboarding.vo.PaymentResponse;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/demo/payments")
@ConditionalOnProperty(prefix = "app.payment", name = "demo-enabled", havingValue = "true")
public class DemoPaymentController {
    private final DemoPaymentService service;

    public DemoPaymentController(DemoPaymentService service) {
        this.service = service;
    }

    @PostMapping("/orders/{orderId}")
    public ApiResponse<PaymentResponse> create(@AuthenticationPrincipal Jwt jwt,
            @Positive @PathVariable long orderId) {
        return ApiResponse.success(service.createPayment(CurrentUser.id(jwt), orderId));
    }

    @PostMapping("/{outTradeNo}/complete")
    public ApiResponse<PaymentResponse> complete(@AuthenticationPrincipal Jwt jwt,
            @PathVariable String outTradeNo) {
        return ApiResponse.success(service.completeDemo(CurrentUser.id(jwt), outTradeNo));
    }
}
