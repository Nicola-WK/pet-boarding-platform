package com.petboarding.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.mapping.BoundSql;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardingServiceMapperSqlTest {
    @Test
    void visibleListSqlRequiresApprovedProviderAndActiveService() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.addMapper(BoardingServiceMapper.class);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("providerId", null);
        parameters.put("petType", "dog");
        parameters.put("keyword", "home");
        parameters.put("minPrice", new BigDecimal("50"));
        parameters.put("maxPrice", new BigDecimal("150"));
        parameters.put("size", 20);
        parameters.put("offset", 0L);

        BoundSql sql = configuration.getMappedStatement(
                BoardingServiceMapper.class.getName() + ".findVisible").getBoundSql(parameters);

        assertTrue(sql.getSql().contains("JOIN provider p ON p.id = s.provider_id"));
        assertTrue(sql.getSql().contains("s.status = 1 AND p.audit_status = 1"));
        assertTrue(sql.getSql().contains("s.pet_type = ?"));
        assertTrue(sql.getSql().contains("s.price >= ?"));
        assertTrue(sql.getSql().contains("s.price <= ?"));
        assertTrue(sql.getSql().contains("LIMIT ? OFFSET ?"));
    }
}
