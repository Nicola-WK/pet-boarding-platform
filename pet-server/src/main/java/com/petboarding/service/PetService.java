package com.petboarding.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.petboarding.dto.PetRequest;
import com.petboarding.entity.Pet;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.mapper.PetMapper;
import com.petboarding.vo.PetResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PetService {
    private final PetMapper mapper;
    private final UserService userService;

    public PetService(PetMapper mapper, UserService userService) {
        this.mapper = mapper;
        this.userService = userService;
    }

    @Transactional
    public PetResponse create(long userId, PetRequest request) {
        userService.requireActive(userId);
        Pet pet = new Pet();
        pet.setUserId(userId);
        pet.setDeleted(0);
        copy(request, pet);
        mapper.insert(pet);
        return PetResponse.from(pet);
    }

    @Transactional(readOnly = true)
    public List<PetResponse> list(long userId) {
        userService.requireActive(userId);
        return mapper.selectList(new LambdaQueryWrapper<Pet>()
                        .eq(Pet::getUserId, userId)
                        .eq(Pet::getDeleted, 0)
                        .orderByDesc(Pet::getCreatedAt, Pet::getId))
                .stream().map(PetResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PetResponse get(long userId, long petId) {
        userService.requireActive(userId);
        Pet pet = mapper.selectOne(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getId, petId)
                .eq(Pet::getUserId, userId)
                .eq(Pet::getDeleted, 0));
        if (pet == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return PetResponse.from(pet);
    }

    @Transactional
    public PetResponse update(long userId, long petId, PetRequest request) {
        userService.requireActive(userId);
        LambdaUpdateWrapper<Pet> update = new LambdaUpdateWrapper<Pet>()
                .set(Pet::getName, request.name())
                .set(Pet::getAvatar, request.avatar())
                .set(Pet::getType, request.type())
                .set(Pet::getBreed, request.breed())
                .set(Pet::getGender, request.gender() == null ? 0 : request.gender())
                .set(Pet::getBirthday, request.birthday())
                .set(Pet::getWeight, request.weight())
                .set(Pet::getVaccine, request.vaccine())
                .set(Pet::getCharacter, request.character())
                .set(Pet::getDiet, request.diet())
                .set(Pet::getRemark, request.remark())
                .eq(Pet::getId, petId)
                .eq(Pet::getUserId, userId)
                .eq(Pet::getDeleted, 0);
        if (mapper.update(null, update) != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return get(userId, petId);
    }

    @Transactional
    public void delete(long userId, long petId) {
        userService.requireActive(userId);
        int updated = mapper.update(null, new LambdaUpdateWrapper<Pet>()
                .set(Pet::getDeleted, 1)
                .eq(Pet::getId, petId)
                .eq(Pet::getUserId, userId)
                .eq(Pet::getDeleted, 0));
        if (updated != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
    }

    private void copy(PetRequest request, Pet pet) {
        pet.setName(request.name());
        pet.setAvatar(request.avatar());
        pet.setType(request.type());
        pet.setBreed(request.breed());
        pet.setGender(request.gender() == null ? 0 : request.gender());
        pet.setBirthday(request.birthday());
        pet.setWeight(request.weight());
        pet.setVaccine(request.vaccine());
        pet.setCharacter(request.character());
        pet.setDiet(request.diet());
        pet.setRemark(request.remark());
    }
}
