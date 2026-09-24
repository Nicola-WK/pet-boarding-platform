package com.petboarding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("boarding_order")
public class BoardingOrder {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long petId;
    private Long providerId;
    private Long serviceId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer boardingDays;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private Integer status;
    private String remark;
    private LocalDateTime expiresAt;
    private LocalDateTime paidAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    private LocalDateTime canceledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
