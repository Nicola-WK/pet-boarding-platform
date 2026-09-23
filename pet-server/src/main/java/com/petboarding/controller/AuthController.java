package com.petboarding.controller;

import com.petboarding.dto.WechatLoginRequest;
import com.petboarding.service.AuthService;
import com.petboarding.vo.ApiResponse;
import com.petboarding.vo.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/wechat")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody WechatLoginRequest request) {
        return ApiResponse.success(service.login(request.code()));
    }
}
