package com.petboarding.controller;

import com.petboarding.config.JwtProperties;
import com.petboarding.mapper.BoardingOrderMapper;
import com.petboarding.mapper.BoardingServiceMapper;
import com.petboarding.mapper.PaymentRecordMapper;
import com.petboarding.mapper.ReviewMapper;
import com.petboarding.mapper.PetMapper;
import com.petboarding.mapper.ProviderMapper;
import com.petboarding.mapper.UserMapper;
import com.petboarding.security.SecurityConfig;
import com.petboarding.service.DemoPaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DemoPaymentController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties(JwtProperties.class)
@TestPropertySource(properties = {
        "app.jwt.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.jwt.issuer=https://pet.example.test",
        "app.jwt.ttl=PT1H",
        "app.payment.demo-enabled=true"
})
class DemoPaymentWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean DemoPaymentService service;
    @MockitoBean BoardingOrderMapper orderMapper;
    @MockitoBean BoardingServiceMapper serviceMapper;
    @MockitoBean PaymentRecordMapper paymentMapper;
    @MockitoBean ReviewMapper reviewMapper;
    @MockitoBean PetMapper petMapper;
    @MockitoBean ProviderMapper providerMapper;
    @MockitoBean UserMapper userMapper;

    @Test
    void paymentCreationRequiresLogin() throws Exception {
        mvc.perform(post("/demo/payments/orders/41"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101));
    }

    @Test
    void demoPaymentUsesJwtOwner() throws Exception {
        mvc.perform(post("/demo/payments/orders/41")
                        .with(jwt().jwt(token -> token.subject("7"))))
                .andExpect(status().isOk());
        verify(service).createPayment(7L, 41L);

        mvc.perform(post("/demo/payments/DEMO123/complete")
                        .with(jwt().jwt(token -> token.subject("7"))))
                .andExpect(status().isOk());
        verify(service).completeDemo(7L, "DEMO123");
    }
}
