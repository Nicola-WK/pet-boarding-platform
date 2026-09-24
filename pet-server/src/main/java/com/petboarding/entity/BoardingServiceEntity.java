package com.petboarding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("boarding_service")
public class BoardingServiceEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long providerId;
    private String name;
    private BigDecimal price;
    private String petType;
    private Integer capacity;
    private String description;
    private String images;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
