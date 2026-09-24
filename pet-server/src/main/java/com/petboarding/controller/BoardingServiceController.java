package com.petboarding.controller;

import com.petboarding.service.BoardingServiceService;
import com.petboarding.vo.ApiResponse;
import com.petboarding.vo.BoardingServiceResponse;
import com.petboarding.vo.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Validated
@RestController
public class BoardingServiceController {
    private final BoardingServiceService service;

    public BoardingServiceController(BoardingServiceService service) {
        this.service = service;
    }

    @GetMapping("/services")
    public ApiResponse<PageResponse<BoardingServiceResponse>> list(
            @RequestParam(required = false) String petType,
            @Size(max = 80) @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @Min(1) @RequestParam(defaultValue = "1") int page,
            @Min(1) @Max(50) @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.list(null, petType, keyword, minPrice, maxPrice, page, size));
    }

    @GetMapping("/services/{id}")
    public ApiResponse<BoardingServiceResponse> get(@Positive @PathVariable long id) {
        return ApiResponse.success(service.get(id));
    }

    @GetMapping("/providers/{id}/services")
    public ApiResponse<PageResponse<BoardingServiceResponse>> byProvider(
            @Positive @PathVariable long id,
            @Min(1) @RequestParam(defaultValue = "1") int page,
            @Min(1) @Max(50) @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.list(id, null, null, null, null, page, size));
    }
}
