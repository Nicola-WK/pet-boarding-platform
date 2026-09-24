package com.petboarding.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petboarding.dto.ReviewRequest;
import com.petboarding.entity.BoardingOrder;
import com.petboarding.entity.Review;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.mapper.BoardingOrderMapper;
import com.petboarding.mapper.ReviewMapper;
import com.petboarding.vo.PageResponse;
import com.petboarding.vo.ReviewResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {
    private static final int COMPLETED = 3;
    private static final int MAX_PAGE_SIZE = 50;

    private final ReviewMapper mapper;
    private final BoardingOrderMapper orderMapper;
    private final UserService userService;
    private final ProviderService providerService;
    private final BoardingServiceService boardingServiceService;
    private final ObjectMapper objectMapper;

    public ReviewService(ReviewMapper mapper, BoardingOrderMapper orderMapper,
                         UserService userService, ProviderService providerService,
                         BoardingServiceService boardingServiceService, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.orderMapper = orderMapper;
        this.userService = userService;
        this.providerService = providerService;
        this.boardingServiceService = boardingServiceService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ReviewResponse create(long userId, long orderId, ReviewRequest request) {
        userService.requireActive(userId);
        BoardingOrder order = orderMapper.lockOrder(orderId);
        if (order == null || !Long.valueOf(userId).equals(order.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!Integer.valueOf(COMPLETED).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        if (mapper.lockByOrderId(orderId) != null) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        Review review = new Review();
        review.setOrderId(orderId);
        review.setUserId(userId);
        review.setProviderId(order.getProviderId());
        review.setServiceId(order.getServiceId());
        review.setScore(request.score());
        review.setContent(request.content());
        review.setImages(encodeImages(request.images()));
        mapper.insert(review);
        return response(mapper.selectById(review.getId()));
    }

    @Transactional(readOnly = true)
    public ReviewResponse getMine(long userId, long orderId) {
        userService.requireActive(userId);
        Review review = mapper.selectOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, orderId)
                .eq(Review::getUserId, userId));
        if (review == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        return response(review);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> listByProvider(long providerId, int page, int size) {
        providerService.getApproved(providerId);
        return page(new LambdaQueryWrapper<Review>()
                .eq(Review::getProviderId, providerId), page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> listByService(long serviceId, int page, int size) {
        boardingServiceService.get(serviceId);
        return page(new LambdaQueryWrapper<Review>()
                .eq(Review::getServiceId, serviceId), page, size);
    }

    private PageResponse<ReviewResponse> page(LambdaQueryWrapper<Review> query,
                                              int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        long total = mapper.selectCount(query);
        long offset = ((long) page - 1) * size;
        List<ReviewResponse> items = mapper.selectList(query
                .orderByDesc(Review::getCreatedAt, Review::getId)
                .last("LIMIT " + size + " OFFSET " + offset))
                .stream().map(this::response).toList();
        return new PageResponse<>(items, total, page, size);
    }

    private String encodeImages(List<String> images) {
        try {
            return objectMapper.writeValueAsString(images == null ? List.of() : images);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not encode review images", ex);
        }
    }

    private ReviewResponse response(Review review) {
        try {
            List<String> images = review.getImages() == null ? List.of()
                    : objectMapper.readValue(review.getImages(), new TypeReference<List<String>>() {});
            return ReviewResponse.from(review, images);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not decode review images", ex);
        }
    }
}
