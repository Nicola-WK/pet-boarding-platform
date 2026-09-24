package com.petboarding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petboarding.entity.Review;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ReviewMapper extends BaseMapper<Review> {
    @Select("SELECT * FROM review WHERE order_id = #{orderId} FOR UPDATE")
    Review lockByOrderId(@Param("orderId") long orderId);
}
