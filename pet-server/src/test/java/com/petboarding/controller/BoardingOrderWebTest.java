package com.petboarding.controller;

import com.petboarding.config.JwtProperties;
import com.petboarding.mapper.BoardingOrderMapper;
import com.petboarding.mapper.PaymentRecordMapper;
import com.petboarding.mapper.BoardingServiceMapper;
import com.petboarding.mapper.PetMapper;
import com.petboarding.mapper.ProviderMapper;
import com.petboarding.mapper.UserMapper;
import com.petboarding.security.SecurityConfig;
import com.petboarding.service.BoardingOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({BoardingOrderController.class, BoardingOrderProviderController.class})
@Import(SecurityConfig.class)
@EnableConfigurationProperties(JwtProperties.class)
@TestPropertySource(properties = {
        "app.jwt.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.jwt.issuer=https://pet.example.test",
        "app.jwt.ttl=PT1H"
})
class BoardingOrderWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean BoardingOrderService service;
    @MockitoBean PaymentRecordMapper paymentRecordMapper;
    @MockitoBean BoardingOrderMapper orderMapper;
    @MockitoBean BoardingServiceMapper serviceMapper;
    @MockitoBean ProviderMapper providerMapper;
    @MockitoBean PetMapper petMapper;
    @MockitoBean UserMapper userMapper;

    @Test
    void orderListRequiresAuthentication() throws Exception {
        mvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101));
    }

    @Test
    void createUsesJwtUserId() throws Exception {
        LocalDate start = LocalDate.now(java.time.ZoneId.of("Asia/Shanghai")).plusDays(1);
        String body = "{\"petId\":11,\"serviceId\":21,\"startDate\":\"" + start
                + "\",\"endDate\":\"" + start.plusDays(2) + "\"}";
        mvc.perform(post("/orders").with(jwt().jwt(token -> token.subject("7")))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        verify(service).create(eq(7L), any());
    }

    @Test
    void invalidOrderUsesUnifiedValidationError() throws Exception {
        mvc.perform(post("/orders").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"petId\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));
    }

    @Test
    void providerOrderIsPrivate() throws Exception {
        mvc.perform(get("/provider/orders"))
                .andExpect(status().isUnauthorized());
    }
}
