package com.petboarding.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import com.petboarding.entity.PaymentRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(@JsonSerialize(using = ToStringSerializer.class) Long id, @JsonSerialize(using = ToStringSerializer.class) Long orderId, String outTradeNo, BigDecimal amount,
                              Integer status, LocalDateTime paidAt, LocalDateTime createdAt) {
    public static PaymentResponse from(PaymentRecord record) {
        return new PaymentResponse(record.getId(), record.getOrderId(), record.getOutTradeNo(),
                record.getAmount(), record.getStatus(), record.getPaidAt(), record.getCreatedAt());
    }
}
