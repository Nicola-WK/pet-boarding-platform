package com.petboarding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petboarding.entity.BoardingOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface BoardingOrderMapper extends BaseMapper<BoardingOrder> {
    @Select("SELECT id FROM boarding_service WHERE id = #{serviceId} FOR UPDATE")
    Long lockService(@Param("serviceId") long serviceId);

    @Select("SELECT id FROM boarding_order WHERE pet_id = #{petId} " +
            "AND start_at < #{endAt} AND end_at > #{startAt} " +
            "AND (status IN (1, 2) OR (status = 0 AND expires_at > #{now})) LIMIT 1 FOR UPDATE")
    List<Long> findPetReservations(@Param("petId") long petId,
                                   @Param("startAt") LocalDateTime startAt,
                                   @Param("endAt") LocalDateTime endAt,
                                   @Param("now") LocalDateTime now);

    @Select("""
            SELECT id FROM boarding_order
            WHERE service_id = #{serviceId}
              AND start_at < #{endAt} AND end_at > #{startAt}
              AND (status IN (1, 2) OR (status = 0 AND expires_at > #{now}))
            LIMIT #{limit} FOR UPDATE
            """)
    List<Long> findReservedIds(@Param("serviceId") long serviceId,
                       @Param("startAt") LocalDateTime startAt,
                       @Param("endAt") LocalDateTime endAt,
                       @Param("now") LocalDateTime now,
                               @Param("limit") int limit);
}
