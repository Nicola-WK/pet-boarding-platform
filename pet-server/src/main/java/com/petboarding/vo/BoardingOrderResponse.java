package com.petboarding.vo;

import com.petboarding.entity.BoardingOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BoardingOrderResponse(Long id, String orderNo, Long userId, Long petId, Long providerId,
                                    Long serviceId, LocalDateTime startAt, LocalDateTime endAt,
                                    Integer boardingDays, BigDecimal unitPrice, BigDecimal totalAmount,
                                    Integer status, String remark, LocalDateTime expiresAt,
                                    LocalDateTime paidAt, LocalDateTime confirmedAt,
                                    LocalDateTime completedAt, LocalDateTime canceledAt,
                                    LocalDateTime createdAt) {
    public static BoardingOrderResponse from(BoardingOrder order) {
        return new BoardingOrderResponse(order.getId(), order.getOrderNo(), order.getUserId(), order.getPetId(),
                order.getProviderId(), order.getServiceId(), order.getStartAt(), order.getEndAt(),
                order.getBoardingDays(), order.getUnitPrice(), order.getTotalAmount(),
                order.getStatus(), order.getRemark(), order.getExpiresAt(), order.getPaidAt(),
                order.getConfirmedAt(), order.getCompletedAt(), order.getCanceledAt(),
                order.getCreatedAt());
    }
}
