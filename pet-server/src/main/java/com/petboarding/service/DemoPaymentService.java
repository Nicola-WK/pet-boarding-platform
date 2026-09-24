package com.petboarding.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.petboarding.entity.BoardingOrder;
import com.petboarding.entity.PaymentRecord;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.mapper.BoardingOrderMapper;
import com.petboarding.mapper.PaymentRecordMapper;
import com.petboarding.vo.PaymentResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "app.payment", name = "demo-enabled", havingValue = "true")
public class DemoPaymentService implements PaymentService {
    private static final int UNPAID = 0;
    private static final int PENDING_CONFIRMATION = 1;
    private static final int CREATED = 0;
    private static final int PAID = 1;
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    private final PaymentRecordMapper paymentMapper;
    private final BoardingOrderMapper orderMapper;
    private final UserService userService;

    public DemoPaymentService(PaymentRecordMapper paymentMapper, BoardingOrderMapper orderMapper,
                              UserService userService) {
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
        this.userService = userService;
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(long userId, long orderId) {
        userService.requireActive(userId);
        BoardingOrder order = ownedOrder(userId, orderId);
        requirePayable(order, LocalDateTime.now(BUSINESS_ZONE));
        PaymentRecord existing = paymentMapper.lockPendingByOrderId(orderId);
        if (existing != null) return PaymentResponse.from(existing);

        PaymentRecord record = new PaymentRecord();
        record.setOrderId(orderId);
        record.setOutTradeNo("DEMO" + UUID.randomUUID().toString().replace("-", ""));
        record.setAmount(order.getTotalAmount());
        record.setStatus(CREATED);
        paymentMapper.insert(record);
        return PaymentResponse.from(paymentMapper.selectById(record.getId()));
    }

    @Transactional
    public PaymentResponse completeDemo(long userId, String outTradeNo) {
        userService.requireActive(userId);
        PaymentRecord candidate = findByTradeNo(outTradeNo);
        BoardingOrder order = ownedOrder(userId, candidate.getOrderId());
        return settle(candidate, order, candidate.getAmount());
    }

    @Override
    @Transactional
    public PaymentResponse notify(String outTradeNo, BigDecimal paidAmount) {
        PaymentRecord candidate = findByTradeNo(outTradeNo);
        BoardingOrder order = lockOrder(candidate.getOrderId());
        return settle(candidate, order, paidAmount);
    }

    private PaymentResponse settle(PaymentRecord candidate, BoardingOrder order,
                                   BigDecimal paidAmount) {
        // The order is locked first in every payment path, matching createPayment's lock order.
        PaymentRecord record = paymentMapper.lockByTradeNo(candidate.getOutTradeNo());
        if (record == null || !record.getOrderId().equals(order.getId())
                || paidAmount == null || record.getAmount().compareTo(paidAmount) != 0
                || record.getAmount().compareTo(order.getTotalAmount()) != 0) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        if (Integer.valueOf(PAID).equals(record.getStatus())
                && Integer.valueOf(PENDING_CONFIRMATION).equals(order.getStatus())) {
            return PaymentResponse.from(record);
        }
        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);
        if (!Integer.valueOf(CREATED).equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        requirePayable(order, now);
        int orderUpdated = orderMapper.update(null, new LambdaUpdateWrapper<BoardingOrder>()
                .set(BoardingOrder::getStatus, PENDING_CONFIRMATION)
                .set(BoardingOrder::getPaidAt, now)
                .eq(BoardingOrder::getId, order.getId())
                .eq(BoardingOrder::getStatus, UNPAID)
                .gt(BoardingOrder::getExpiresAt, now));
        if (orderUpdated != 1) throw new BusinessException(ErrorCode.CONFLICT);
        int paymentUpdated = paymentMapper.update(null, new LambdaUpdateWrapper<PaymentRecord>()
                .set(PaymentRecord::getStatus, PAID)
                .set(PaymentRecord::getPaidAt, now)
                .eq(PaymentRecord::getId, record.getId())
                .eq(PaymentRecord::getStatus, CREATED));
        if (paymentUpdated != 1) throw new BusinessException(ErrorCode.CONFLICT);
        return PaymentResponse.from(paymentMapper.selectById(record.getId()));
    }

    private PaymentRecord findByTradeNo(String outTradeNo) {
        PaymentRecord record = paymentMapper.selectOne(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getOutTradeNo, outTradeNo));
        if (record == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        return record;
    }

    private BoardingOrder ownedOrder(long userId, long orderId) {
        BoardingOrder order = lockOrder(orderId);
        if (!Long.valueOf(userId).equals(order.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return order;
    }

    private BoardingOrder lockOrder(long orderId) {
        BoardingOrder order = orderMapper.lockOrder(orderId);
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        return order;
    }

    private void requirePayable(BoardingOrder order, LocalDateTime now) {
        if (!Integer.valueOf(UNPAID).equals(order.getStatus())
                || order.getExpiresAt() == null || !order.getExpiresAt().isAfter(now)) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
    }
}
