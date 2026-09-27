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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocalDemoController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties(JwtProperties.class)
@TestPropertySource(properties = {
        "app.jwt.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.jwt.issuer=https://pet.example.test",
        "app.jwt.ttl=PT1H",
        "app.demo.enabled=false"
})
class LocalDemoDisabledWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean BoardingOrderMapper orderMapper;
    @MockitoBean BoardingServiceMapper serviceMapper;
    @MockitoBean PaymentRecordMapper paymentMapper;
    @MockitoBean ReviewMapper reviewMapper;
    @MockitoBean PetMapper petMapper;
    @MockitoBean ProviderMapper providerMapper;
    @MockitoBean UserMapper userMapper;

    @Test
    void demoLoginRouteIsAbsentByDefault() throws Exception {
        mvc.perform(post("/demo/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"customer\"}"))
                .andExpect(status().isNotFound());
    }
}
