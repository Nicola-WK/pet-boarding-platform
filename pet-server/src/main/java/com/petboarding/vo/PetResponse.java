package com.petboarding.vo;

import com.petboarding.entity.Pet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PetResponse(Long id, String name, String avatar, String type, String breed,
                          Integer gender, LocalDateTime birthday, BigDecimal weight,
                          String vaccine, String character, String diet, String remark) {
    public static PetResponse from(Pet pet) {
        return new PetResponse(pet.getId(), pet.getName(), pet.getAvatar(), pet.getType(),
                pet.getBreed(), pet.getGender(), pet.getBirthday(), pet.getWeight(),
                pet.getVaccine(), pet.getCharacter(), pet.getDiet(), pet.getRemark());
    }
}
