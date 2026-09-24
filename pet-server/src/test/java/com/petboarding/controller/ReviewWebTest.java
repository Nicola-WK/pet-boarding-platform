package com.petboarding.controller;

import com.petboarding.config.JwtProperties;
import com.petboarding.mapper.BoardingOrderMapper;
import com.petboarding.mapper.BoardingServiceMapper;
import com.petboarding.mapper.PaymentRecordMapper;
import com.petboarding.mapper.PetMapper;
import com.petboarding.mapper.ProviderMapper;
import com.petboarding.mapper.ReviewMapper;
import com.petboarding.mapper.UserMapper;
import com.petboarding.security.SecurityConfig;
import com.petboarding.service.ReviewService;
import com.petboarding.vo.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties(JwtProperties.class)
@TestPropertySource(properties = {
        "app.jwt.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.jwt.issuer=https://pet.example.test",
        "app.jwt.ttl=PT1H"
})
class ReviewWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean ReviewService service;
    @MockitoBean ReviewMapper reviewMapper;
    @MockitoBean BoardingOrderMapper orderMapper;
    @MockitoBean BoardingServiceMapper serviceMapper;
    @MockitoBean PaymentRecordMapper paymentMapper;
    @MockitoBean PetMapper petMapper;
    @MockitoBean ProviderMapper providerMapper;
    @MockitoBean UserMapper userMapper;

    @Test
    void approvedProviderReviewsArePublic() throws Exception {
        when(service.listByProvider(31L, 1, 20))
                .thenReturn(new PageResponse<>(List.of(), 0, 1, 20));
        mvc.perform(get("/providers/31/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void serviceReviewsArePublic() throws Exception {
        mvc.perform(get("/services/21/reviews"))
                .andExpect(status().isOk());
        verify(service).listByService(21L, 1, 20);
    }

    @Test
    void creatingReviewRequiresLogin() throws Exception {
        mvc.perform(post("/orders/41/review").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":5}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createUsesJwtOwnerAndValidatesScore() throws Exception {
        mvc.perform(post("/orders/41/review")
                        .with(jwt().jwt(token -> token.subject("7")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":5,\"content\":\"Great care\"}"))
                .andExpect(status().isOk());
        verify(service).create(eq(7L), eq(41L), any());

        mvc.perform(post("/orders/41/review")
                        .with(jwt().jwt(token -> token.subject("7")))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"score\":6}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));
    }
}
