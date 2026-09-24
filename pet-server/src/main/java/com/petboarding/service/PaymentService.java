package com.petboarding.service;

import com.petboarding.vo.PaymentResponse;

import java.math.BigDecimal;

public interface PaymentService {
    PaymentResponse createPayment(long userId, long orderId);

    /** Called only after a payment result has been verified by a trusted adapter. */
    PaymentResponse notify(String outTradeNo, BigDecimal paidAmount);
}
