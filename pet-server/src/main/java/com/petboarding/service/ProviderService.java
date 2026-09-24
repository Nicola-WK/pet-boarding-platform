package com.petboarding.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petboarding.config.AdminProperties;
import com.petboarding.dto.ProviderApplicationRequest;
import com.petboarding.dto.ProviderAuditRequest;
import com.petboarding.entity.Provider;
import com.petboarding.entity.User;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.mapper.ProviderMapper;
import com.petboarding.vo.PageResponse;
import com.petboarding.vo.ProviderPrivateResponse;
import com.petboarding.vo.ProviderPublicResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Service
public class ProviderService {
    private static final int PENDING = 0;
    private static final int APPROVED = 1;
    private static final int REJECTED = 2;
    private static final int MAX_PAGE_SIZE = 50;

    private final ProviderMapper mapper;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final Set<String> adminOpenids;

    public ProviderService(ProviderMapper mapper, UserService userService,
                           ObjectMapper objectMapper, AdminProperties adminProperties) {
        this.mapper = mapper;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.adminOpenids = adminProperties.allowedOpenids();
    }

    @Transactional
    public ProviderPrivateResponse apply(long userId, ProviderApplicationRequest request) {
        userService.requireActive(userId);
        validateLocation(request);
        Provider existing = mapper.selectOne(new LambdaQueryWrapper<Provider>()
                .eq(Provider::getOwnerUserId, userId));
        if (existing != null) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }

        Provider provider = new Provider();
        provider.setOwnerUserId(userId);
        provider.setAuditStatus(PENDING);
        copy(request, provider);
        mapper.insert(provider);
        return privateResponse(mapper.selectById(provider.getId()));
    }

    @Transactional(readOnly = true)
    public ProviderPrivateResponse mine(long userId) {
        userService.requireActive(userId);
        Provider provider = mapper.selectOne(new LambdaQueryWrapper<Provider>()
                .eq(Provider::getOwnerUserId, userId));
        if (provider == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return privateResponse(provider);
    }

    @Transactional
    public ProviderPrivateResponse updateMine(long userId, ProviderApplicationRequest request) {
        userService.requireActive(userId);
        validateLocation(request);
        Provider existing = mapper.selectOne(new LambdaQueryWrapper<Provider>()
                .eq(Provider::getOwnerUserId, userId));
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        int updated = mapper.update(null, new LambdaUpdateWrapper<Provider>()
                .set(Provider::getName, request.name())
                .set(Provider::getContactPhone, request.contactPhone())
                .set(Provider::getAddress, request.address())
                .set(Provider::getLatitude, request.latitude())
                .set(Provider::getLongitude, request.longitude())
                .set(Provider::getDescription, request.description())
                .set(Provider::getImages, encodeImages(request.images()))
                .set(Provider::getLicenseImage, request.licenseImage())
                .set(Provider::getAuditStatus, PENDING)
                .set(Provider::getAuditReason, null)
                .set(Provider::getAuditedAt, null)
                .eq(Provider::getId, existing.getId())
                .eq(Provider::getOwnerUserId, userId));
        if (updated != 1) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        return mine(userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProviderPublicResponse> listApproved(String keyword, int page, int size) {
        checkPage(page, size);
        LambdaQueryWrapper<Provider> query = new LambdaQueryWrapper<Provider>()
                .eq(Provider::getAuditStatus, APPROVED);
        if (keyword != null && !keyword.isBlank()) {
            String search = keyword.trim();
            query.and(condition -> condition.like(Provider::getName, search)
                    .or().like(Provider::getAddress, search));
        }
        long total = mapper.selectCount(query);
        long offset = ((long) page - 1) * size;
        List<ProviderPublicResponse> items = mapper.selectList(query
                        .orderByDesc(Provider::getCreatedAt, Provider::getId)
                        .last("LIMIT " + size + " OFFSET " + offset))
                .stream().map(this::publicResponse).toList();
        return new PageResponse<>(items, total, page, size);
    }

    @Transactional(readOnly = true)
    public ProviderPublicResponse getApproved(long providerId) {
        Provider provider = mapper.selectOne(new LambdaQueryWrapper<Provider>()
                .eq(Provider::getId, providerId)
                .eq(Provider::getAuditStatus, APPROVED));
        if (provider == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return publicResponse(provider);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProviderPrivateResponse> pending(long adminUserId, int page, int size) {
        requireAdmin(adminUserId);
        checkPage(page, size);
        LambdaQueryWrapper<Provider> query = new LambdaQueryWrapper<Provider>()
                .eq(Provider::getAuditStatus, PENDING);
        long total = mapper.selectCount(query);
        long offset = ((long) page - 1) * size;
        List<ProviderPrivateResponse> items = mapper.selectList(query
                        .orderByAsc(Provider::getCreatedAt, Provider::getId)
                        .last("LIMIT " + size + " OFFSET " + offset))
                .stream().map(this::privateResponse).toList();
        return new PageResponse<>(items, total, page, size);
    }

    @Transactional
    public ProviderPrivateResponse audit(long adminUserId, long providerId,
                                         ProviderAuditRequest request) {
        requireAdmin(adminUserId);
        if (Boolean.FALSE.equals(request.approved())
                && (request.reason() == null || request.reason().isBlank())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Provider provider = mapper.selectById(providerId);
        if (provider == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!Integer.valueOf(PENDING).equals(provider.getAuditStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        int status = Boolean.TRUE.equals(request.approved()) ? APPROVED : REJECTED;
        int updated = mapper.update(null, new LambdaUpdateWrapper<Provider>()
                .set(Provider::getAuditStatus, status)
                .set(Provider::getAuditReason, status == REJECTED ? request.reason().trim() : null)
                .set(Provider::getAuditedAt, LocalDateTime.now(ZoneId.of("Asia/Shanghai")))
                .eq(Provider::getId, providerId)
                .eq(Provider::getAuditStatus, PENDING));
        if (updated != 1) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        return privateResponse(mapper.selectById(providerId));
    }

    private void requireAdmin(long userId) {
        User user = userService.requireActive(userId);
        if (!adminOpenids.contains(user.getOpenid())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private void checkPage(int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateLocation(ProviderApplicationRequest request) {
        if ((request.latitude() == null) != (request.longitude() == null)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void copy(ProviderApplicationRequest request, Provider provider) {
        provider.setName(request.name());
        provider.setContactPhone(request.contactPhone());
        provider.setAddress(request.address());
        provider.setLatitude(request.latitude());
        provider.setLongitude(request.longitude());
        provider.setDescription(request.description());
        provider.setImages(encodeImages(request.images()));
        provider.setLicenseImage(request.licenseImage());
    }

    private String encodeImages(List<String> images) {
        try {
            return objectMapper.writeValueAsString(images == null ? List.of() : images);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not encode provider images", ex);
        }
    }

    private List<String> decodeImages(String json) {
        if (json == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not decode provider images", ex);
        }
    }

    private ProviderPublicResponse publicResponse(Provider provider) {
        return ProviderPublicResponse.from(provider, decodeImages(provider.getImages()));
    }

    private ProviderPrivateResponse privateResponse(Provider provider) {
        return ProviderPrivateResponse.from(provider, decodeImages(provider.getImages()));
    }
}