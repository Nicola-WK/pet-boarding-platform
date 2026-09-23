package com.petboarding.controller;

import com.petboarding.dto.PetRequest;
import com.petboarding.security.CurrentUser;
import com.petboarding.service.PetService;
import com.petboarding.vo.ApiResponse;
import com.petboarding.vo.PetResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/pets")
public class PetController {
    private final PetService service;

    public PetController(PetService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<PetResponse> create(@AuthenticationPrincipal Jwt jwt,
                                           @Valid @RequestBody PetRequest request) {
        return ApiResponse.success(service.create(CurrentUser.id(jwt), request));
    }

    @GetMapping
    public ApiResponse<List<PetResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.list(CurrentUser.id(jwt)));
    }

    @GetMapping("/{id}")
    public ApiResponse<PetResponse> get(@AuthenticationPrincipal Jwt jwt,
                                        @Positive @PathVariable Long id) {
        return ApiResponse.success(service.get(CurrentUser.id(jwt), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<PetResponse> update(@AuthenticationPrincipal Jwt jwt,
                                           @Positive @PathVariable Long id,
                                           @Valid @RequestBody PetRequest request) {
        return ApiResponse.success(service.update(CurrentUser.id(jwt), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal Jwt jwt,
                                    @Positive @PathVariable Long id) {
        service.delete(CurrentUser.id(jwt), id);
        return ApiResponse.success(null);
    }
}
