package com.petboarding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petboarding.entity.BoardingServiceEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

public interface BoardingServiceMapper extends BaseMapper<BoardingServiceEntity> {
    String VISIBLE_FILTER = """
            FROM boarding_service s
            JOIN provider p ON p.id = s.provider_id
            WHERE s.status = 1 AND p.audit_status = 1
            <if test="providerId != null">AND s.provider_id = #{providerId}</if>
            <if test="petType != null">AND s.pet_type = #{petType}</if>
            <if test="keyword != null">AND s.name LIKE CONCAT('%', #{keyword}, '%')</if>
            <if test="minPrice != null">AND s.price &gt;= #{minPrice}</if>
            <if test="maxPrice != null">AND s.price &lt;= #{maxPrice}</if>
            """;

    @Select("<script>SELECT s.* " + VISIBLE_FILTER
            + " ORDER BY s.created_at DESC, s.id DESC LIMIT #{size} OFFSET #{offset}</script>")
    List<BoardingServiceEntity> findVisible(@Param("providerId") Long providerId,
                                            @Param("petType") String petType,
                                            @Param("keyword") String keyword,
                                            @Param("minPrice") BigDecimal minPrice,
                                            @Param("maxPrice") BigDecimal maxPrice,
                                            @Param("size") int size, @Param("offset") long offset);

    @Select("<script>SELECT COUNT(*) " + VISIBLE_FILTER + "</script>")
    long countVisible(@Param("providerId") Long providerId, @Param("petType") String petType,
                      @Param("keyword") String keyword, @Param("minPrice") BigDecimal minPrice,
                      @Param("maxPrice") BigDecimal maxPrice);

    @Select("""
            SELECT s.* FROM boarding_service s
            JOIN provider p ON p.id = s.provider_id
            WHERE s.id = #{id} AND s.status = 1 AND p.audit_status = 1
            """)
    BoardingServiceEntity findVisibleById(@Param("id") long id);

    @Select("SELECT s.* FROM boarding_service s JOIN provider p ON p.id = s.provider_id WHERE s.id = #{id} AND s.status = 1 AND p.audit_status = 1 FOR UPDATE")
    BoardingServiceEntity lockVisibleById(@Param("id") long id);
}
