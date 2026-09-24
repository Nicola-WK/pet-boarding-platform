package com.petboarding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;
    private Long userId;
    private Long providerId;
    private Long serviceId;
    private Integer score;
    private String content;
    private String images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
