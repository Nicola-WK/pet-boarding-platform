package com.petboarding.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import com.petboarding.entity.Pet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PetResponse(@JsonSerialize(using = ToStringSerializer.class) Long id, String name, String avatar, String type, String breed,
                          Integer gender, LocalDateTime birthday, BigDecimal weight,
                          String vaccine, String character, String diet, String remark) {
    public static PetResponse from(Pet pet) {
        return new PetResponse(pet.getId(), pet.getName(), pet.getAvatar(), pet.getType(),
                pet.getBreed(), pet.getGender(), pet.getBirthday(), pet.getWeight(),
                pet.getVaccine(), pet.getCharacter(), pet.getDiet(), pet.getRemark());
    }
}
