package com.petboarding.vo;

import com.petboarding.entity.PaymentRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(Long id, Long orderId, String outTradeNo, BigDecimal amount,
                              Integer status, LocalDateTime paidAt, LocalDateTime createdAt) {
    public static PaymentResponse from(PaymentRecord record) {
        return new PaymentResponse(record.getId(), record.getOrderId(), record.getOutTradeNo(),
                record.getAmount(), record.getStatus(), record.getPaidAt(), record.getCreatedAt());
    }
}
