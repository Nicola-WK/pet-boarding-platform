package com.petboarding.controller;

import com.petboarding.security.CurrentUser;
import com.petboarding.service.UserService;
import com.petboarding.vo.ApiResponse;
import com.petboarding.vo.UserProfile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfile> profile(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(UserProfile.from(service.requireActive(CurrentUser.id(jwt))));
    }
}
