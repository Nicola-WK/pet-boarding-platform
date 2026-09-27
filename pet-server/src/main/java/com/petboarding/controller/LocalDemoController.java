package com.petboarding.controller;

import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.service.LocalDemoService;
import com.petboarding.vo.ApiResponse;
import com.petboarding.vo.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo/auth")
@ConditionalOnProperty(prefix = "app.demo", name = "enabled", havingValue = "true")
public class LocalDemoController {
    private final LocalDemoService service;

    public LocalDemoController(LocalDemoService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletRequest servletRequest) {
        String remote = servletRequest.getRemoteAddr();
        if (!"127.0.0.1".equals(remote) && !"0:0:0:0:0:0:0:1".equals(remote)
                && !"::1".equals(remote)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return ApiResponse.success(service.login(request.role()));
    }

    public record LoginRequest(@Pattern(regexp = "customer|provider") String role) {}
}
