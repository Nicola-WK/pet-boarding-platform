package com.petboarding.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.mapping.BoundSql;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardingOrderMapperSqlTest {
    @Test
    void reservationQueryChecksOverlapAndLocksCurrentRows() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.addMapper(BoardingOrderMapper.class);
        LocalDateTime start = LocalDateTime.of(2026, 10, 1, 0, 0);
        BoundSql sql = configuration.getMappedStatement(
                BoardingOrderMapper.class.getName() + ".findReservedIds")
                .getBoundSql(Map.of("serviceId", 21L, "startAt", start,
                        "endAt", start.plusDays(1), "now", start.minusMinutes(1), "limit", 3));

        assertTrue(sql.getSql().contains("start_at < ? AND end_at > ?"));
        assertTrue(sql.getSql().contains("status IN (1, 2)"));
        assertTrue(sql.getSql().contains("status = 0 AND expires_at > ?"));
        assertTrue(sql.getSql().contains("LIMIT ? FOR UPDATE"));
    }
}
