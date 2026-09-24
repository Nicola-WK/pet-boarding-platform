package com.petboarding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petboarding.entity.Pet;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PetMapper extends BaseMapper<Pet> {
    @Select("SELECT * FROM pet WHERE id = #{petId} AND user_id = #{userId} AND is_deleted = 0 FOR UPDATE")
    Pet lockActiveOwned(@Param("petId") long petId, @Param("userId") long userId);
}
