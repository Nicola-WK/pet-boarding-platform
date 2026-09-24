package com.petboarding.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.petboarding.entity.BoardingOrder;
import com.petboarding.entity.PaymentRecord;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.mapper.BoardingOrderMapper;
import com.petboarding.mapper.PaymentRecordMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoPaymentServiceTest {
    @BeforeAll
    static void metadata() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        TableInfoHelper.initTableInfo(assistant, BoardingOrder.class);
        TableInfoHelper.initTableInfo(assistant, PaymentRecord.class);
    }

    @Mock PaymentRecordMapper paymentMapper;
    @Mock BoardingOrderMapper orderMapper;
    @Mock UserService userService;

    private DemoPaymentService service() {
        return new DemoPaymentService(paymentMapper, orderMapper, userService);
    }

    private BoardingOrder order(long ownerId, int status, LocalDateTime expiresAt) {
        BoardingOrder order = new BoardingOrder();
        order.setId(41L);
        order.setUserId(ownerId);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("176.00"));
        order.setExpiresAt(expiresAt);
        return order;
    }

    private PaymentRecord record(int status) {
        PaymentRecord record = new PaymentRecord();
        record.setId(51L);
        record.setOrderId(41L);
        record.setOutTradeNo("DEMO123");
        record.setAmount(new BigDecimal("176.00"));
        record.setStatus(status);
        return record;
    }

    @Test
    void anotherUserCannotCreatePayment() {
        when(orderMapper.lockOrder(41L)).thenReturn(order(7L, 0, LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai")).plusMinutes(30)));
        BusinessException error = assertThrows(BusinessException.class,
                () -> service().createPayment(8L, 41L));
        assertEquals(ErrorCode.NOT_FOUND, error.errorCode());
        verifyNoInteractions(paymentMapper);
    }

    @Test
    void pendingAttemptIsReturnedWithoutCreatingAnother() {
        when(orderMapper.lockOrder(41L)).thenReturn(order(7L, 0, LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai")).plusMinutes(30)));
        when(paymentMapper.lockPendingByOrderId(41L)).thenReturn(record(0));

        var response = service().createPayment(7L, 41L);

        assertEquals("DEMO123", response.outTradeNo());
        org.mockito.Mockito.verify(paymentMapper, org.mockito.Mockito.never())
                .insert(any(PaymentRecord.class));
    }

    @Test
    void expiredOrderCannotCreatePayment() {
        when(orderMapper.lockOrder(41L)).thenReturn(order(7L, 0, LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai")).minusMinutes(1)));
        BusinessException error = assertThrows(BusinessException.class,
                () -> service().createPayment(7L, 41L));
        assertEquals(ErrorCode.CONFLICT, error.errorCode());
        verifyNoInteractions(paymentMapper);
    }

    @Test
    void completionUpdatesOrderAndPaymentTogether() {
        when(paymentMapper.selectOne(any())).thenReturn(record(0));
        when(orderMapper.lockOrder(41L)).thenReturn(order(7L, 0, LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai")).plusMinutes(30)));
        when(paymentMapper.lockByTradeNo("DEMO123")).thenReturn(record(0));
        when(orderMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(paymentMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(paymentMapper.selectById(51L)).thenReturn(record(1));

        var response = service().completeDemo(7L, "DEMO123");

        assertEquals(1, response.status());
        verify(orderMapper).update(isNull(), any(LambdaUpdateWrapper.class));
        verify(paymentMapper).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void duplicateNotificationIsIdempotent() {
        when(paymentMapper.selectOne(any())).thenReturn(record(1));
        when(orderMapper.lockOrder(41L)).thenReturn(order(7L, 1, LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai")).minusMinutes(1)));
        when(paymentMapper.lockByTradeNo("DEMO123")).thenReturn(record(1));

        var response = service().notify("DEMO123", new BigDecimal("176.00"));

        assertEquals(1, response.status());
        org.mockito.Mockito.verify(orderMapper, org.mockito.Mockito.never())
                .update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void wrongAmountIsRejectedBeforeAnyUpdate() {
        when(paymentMapper.selectOne(any())).thenReturn(record(0));
        when(orderMapper.lockOrder(41L)).thenReturn(order(7L, 0, LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai")).plusMinutes(30)));
        when(paymentMapper.lockByTradeNo("DEMO123")).thenReturn(record(0));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service().notify("DEMO123", new BigDecimal("1.00")));

        assertEquals(ErrorCode.CONFLICT, error.errorCode());
        org.mockito.Mockito.verify(orderMapper, org.mockito.Mockito.never())
                .update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void newAttemptUsesOrderAmountSnapshot() {
        when(orderMapper.lockOrder(41L)).thenReturn(order(7L, 0,
                LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai")).plusMinutes(30)));
        org.mockito.Mockito.doAnswer(invocation -> {
            PaymentRecord inserted = invocation.getArgument(0);
            inserted.setId(51L);
            return 1;
        }).when(paymentMapper).insert(any(PaymentRecord.class));
        when(paymentMapper.selectById(51L)).thenReturn(record(0));

        var response = service().createPayment(7L, 41L);

        org.mockito.ArgumentCaptor<PaymentRecord> captured =
                org.mockito.ArgumentCaptor.forClass(PaymentRecord.class);
        verify(paymentMapper).insert(captured.capture());
        assertEquals(new BigDecimal("176.00"), captured.getValue().getAmount());
        assertEquals(0, captured.getValue().getStatus());
        assertEquals(41L, response.orderId());
    }

    @Test
    void anotherUserCannotCompletePayment() {
        when(paymentMapper.selectOne(any())).thenReturn(record(0));
        when(orderMapper.lockOrder(41L)).thenReturn(order(7L, 0,
                LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai")).plusMinutes(30)));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service().completeDemo(8L, "DEMO123"));

        assertEquals(ErrorCode.NOT_FOUND, error.errorCode());
        org.mockito.Mockito.verify(paymentMapper, org.mockito.Mockito.never())
                .lockByTradeNo("DEMO123");
    }
}
