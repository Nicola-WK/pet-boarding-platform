package com.petboarding.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiIdSerializationTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void bigintIdsAreStringsWhilePaginationTotalRemainsNumeric() throws Exception {
        long id = 9_007_199_254_740_993L;
        var page = new PageResponse<>(List.of(new UserProfile(id, "Pet owner", null, null)), 1, 1, 20);

        var json = mapper.readTree(mapper.writeValueAsString(page));

        assertEquals("9007199254740993", json.path("items").get(0).path("id").asText());
        assertEquals(true, json.path("items").get(0).path("id").isTextual());
        assertEquals(true, json.path("total").isNumber());
    }
}
