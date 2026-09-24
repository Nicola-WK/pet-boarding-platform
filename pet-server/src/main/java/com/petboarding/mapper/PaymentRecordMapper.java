package com.petboarding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petboarding.entity.PaymentRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
    @Select("SELECT * FROM payment_record WHERE order_id = #{orderId} AND status = 0 ORDER BY id DESC LIMIT 1 FOR UPDATE")
    PaymentRecord lockPendingByOrderId(@Param("orderId") long orderId);

    @Select("SELECT * FROM payment_record WHERE out_trade_no = #{outTradeNo} FOR UPDATE")
    PaymentRecord lockByTradeNo(@Param("outTradeNo") String outTradeNo);
}
