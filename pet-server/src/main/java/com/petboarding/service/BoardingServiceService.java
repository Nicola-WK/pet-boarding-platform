package com.petboarding.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petboarding.dto.BoardingServiceRequest;
import com.petboarding.entity.BoardingServiceEntity;
import com.petboarding.entity.Provider;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.mapper.BoardingServiceMapper;
import com.petboarding.mapper.ProviderMapper;
import com.petboarding.vo.BoardingServiceResponse;
import com.petboarding.vo.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BoardingServiceService {
    private static final int ACTIVE = 1;
    private static final int INACTIVE = 0;
    private static final int APPROVED = 1;
    private static final int MAX_PAGE_SIZE = 50;

    private final BoardingServiceMapper mapper;
    private final ProviderMapper providerMapper;
    private final UserService userService;
    private final ProviderService providerService;
    private final ObjectMapper objectMapper;

    public BoardingServiceService(BoardingServiceMapper mapper, ProviderMapper providerMapper,
                                  UserService userService, ProviderService providerService,
                                  ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.providerMapper = providerMapper;
        this.userService = userService;
        this.providerService = providerService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public BoardingServiceResponse create(long userId, BoardingServiceRequest request) {
        Provider provider = requireOwnerProvider(userId);
        requireApproved(provider);
        BoardingServiceEntity entity = new BoardingServiceEntity();
        entity.setProviderId(provider.getId());
        entity.setStatus(ACTIVE);
        copy(request, entity);
        mapper.insert(entity);
        return response(mapper.selectById(entity.getId()));
    }

    @Transactional
    public BoardingServiceResponse update(long userId, long id, BoardingServiceRequest request) {
        Provider provider = requireOwnerProvider(userId);
        BoardingServiceEntity entity = requireOwned(id, provider.getId());
        int updated = mapper.update(null, new LambdaUpdateWrapper<BoardingServiceEntity>()
                .set(BoardingServiceEntity::getName, request.name())
                .set(BoardingServiceEntity::getPrice, request.price())
                .set(BoardingServiceEntity::getPetType, request.petType())
                .set(BoardingServiceEntity::getCapacity, request.capacity())
                .set(BoardingServiceEntity::getDescription, request.description())
                .set(BoardingServiceEntity::getImages, encodeImages(request.images()))
                .eq(BoardingServiceEntity::getId, id)
                .eq(BoardingServiceEntity::getProviderId, provider.getId()));
        if (updated != 1) throw new BusinessException(ErrorCode.CONFLICT);
        return response(mapper.selectById(entity.getId()));
    }

    @Transactional
    public BoardingServiceResponse setActive(long userId, long id, boolean active) {
        Provider provider = requireOwnerProvider(userId);
        requireOwned(id, provider.getId());
        if (active) requireApproved(provider);
        int updated = mapper.update(null, new LambdaUpdateWrapper<BoardingServiceEntity>()
                .set(BoardingServiceEntity::getStatus, active ? ACTIVE : INACTIVE)
                .eq(BoardingServiceEntity::getId, id)
                .eq(BoardingServiceEntity::getProviderId, provider.getId()));
        if (updated != 1) throw new BusinessException(ErrorCode.CONFLICT);
        return response(mapper.selectById(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<BoardingServiceResponse> mine(long userId, int page, int size) {
        Provider provider = requireOwnerProvider(userId);
        checkPage(page, size);
        LambdaQueryWrapper<BoardingServiceEntity> query = new LambdaQueryWrapper<BoardingServiceEntity>()
                .eq(BoardingServiceEntity::getProviderId, provider.getId());
        long total = mapper.selectCount(query);
        long offset = ((long) page - 1) * size;
        List<BoardingServiceResponse> items = mapper.selectList(query
                .orderByDesc(BoardingServiceEntity::getCreatedAt, BoardingServiceEntity::getId)
                .last("LIMIT " + size + " OFFSET " + offset)).stream().map(this::response).toList();
        return new PageResponse<>(items, total, page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<BoardingServiceResponse> list(Long providerId, String petType, String keyword,
                                                      BigDecimal minPrice, BigDecimal maxPrice,
                                                      int page, int size) {
        checkPage(page, size);
        if (providerId != null) providerService.getApproved(providerId);
        if (petType != null && !List.of("dog", "cat", "other").contains(petType)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (minPrice != null && minPrice.signum() < 0 || maxPrice != null && maxPrice.signum() < 0
                || minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        String search = keyword == null || keyword.isBlank() ? null : keyword.trim();
        long total = mapper.countVisible(providerId, petType, search, minPrice, maxPrice);
        long offset = ((long) page - 1) * size;
        List<BoardingServiceResponse> items = mapper.findVisible(providerId, petType, search,
                minPrice, maxPrice, size, offset).stream().map(this::response).toList();
        return new PageResponse<>(items, total, page, size);
    }

    @Transactional(readOnly = true)
    public BoardingServiceResponse get(long id) {
        BoardingServiceEntity entity = mapper.findVisibleById(id);
        if (entity == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        return response(entity);
    }

    private Provider requireOwnerProvider(long userId) {
        userService.requireActive(userId);
        Provider provider = providerMapper.selectOne(new LambdaQueryWrapper<Provider>()
                .eq(Provider::getOwnerUserId, userId));
        if (provider == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        return provider;
    }

    private void requireApproved(Provider provider) {
        if (!Integer.valueOf(APPROVED).equals(provider.getAuditStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private BoardingServiceEntity requireOwned(long id, long providerId) {
        BoardingServiceEntity entity = mapper.selectOne(new LambdaQueryWrapper<BoardingServiceEntity>()
                .eq(BoardingServiceEntity::getId, id)
                .eq(BoardingServiceEntity::getProviderId, providerId));
        if (entity == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        return entity;
    }

    private void checkPage(int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void copy(BoardingServiceRequest request, BoardingServiceEntity entity) {
        entity.setName(request.name());
        entity.setPrice(request.price());
        entity.setPetType(request.petType());
        entity.setCapacity(request.capacity());
        entity.setDescription(request.description());
        entity.setImages(encodeImages(request.images()));
    }

    private String encodeImages(List<String> images) {
        try {
            return objectMapper.writeValueAsString(images == null ? List.of() : images);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not encode service images", ex);
        }
    }

    private BoardingServiceResponse response(BoardingServiceEntity entity) {
        try {
            List<String> images = entity.getImages() == null ? List.of()
                    : objectMapper.readValue(entity.getImages(), new TypeReference<List<String>>() {});
            return BoardingServiceResponse.from(entity, images);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not decode service images", ex);
        }
    }
}
