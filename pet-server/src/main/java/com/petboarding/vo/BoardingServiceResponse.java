package com.petboarding.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import com.petboarding.entity.BoardingServiceEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BoardingServiceResponse(@JsonSerialize(using = ToStringSerializer.class) Long id, @JsonSerialize(using = ToStringSerializer.class) Long providerId, String name, BigDecimal price,
                                      String petType, Integer capacity, String description,
                                      List<String> images, Integer status,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static BoardingServiceResponse from(BoardingServiceEntity entity, List<String> images) {
        return new BoardingServiceResponse(entity.getId(), entity.getProviderId(), entity.getName(),
                entity.getPrice(), entity.getPetType(), entity.getCapacity(), entity.getDescription(),
                images, entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
