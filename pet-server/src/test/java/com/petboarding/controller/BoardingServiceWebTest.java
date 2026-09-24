package com.petboarding.controller;

import com.petboarding.config.JwtProperties;
import com.petboarding.mapper.BoardingOrderMapper;
import com.petboarding.mapper.PaymentRecordMapper;
import com.petboarding.mapper.ReviewMapper;
import com.petboarding.mapper.BoardingServiceMapper;
import com.petboarding.mapper.PetMapper;
import com.petboarding.mapper.ProviderMapper;
import com.petboarding.mapper.UserMapper;
import com.petboarding.security.SecurityConfig;
import com.petboarding.service.BoardingServiceService;
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

import java.math.BigDecimal;
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

@WebMvcTest({BoardingServiceController.class, BoardingServiceOwnerController.class})
@Import(SecurityConfig.class)
@EnableConfigurationProperties(JwtProperties.class)
@TestPropertySource(properties = {
        "app.jwt.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.jwt.issuer=https://pet.example.test",
        "app.jwt.ttl=PT1H"
})
class BoardingServiceWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean BoardingServiceService service;
    @MockitoBean BoardingServiceMapper boardingServiceMapper;
    @MockitoBean PaymentRecordMapper paymentRecordMapper;
    @MockitoBean ReviewMapper reviewMapper;
    @MockitoBean BoardingOrderMapper boardingOrderMapper;
    @MockitoBean ProviderMapper providerMapper;
    @MockitoBean PetMapper petMapper;
    @MockitoBean UserMapper userMapper;

    @Test
    void publicCanBrowseByPetTypeAndPrice() throws Exception {
        when(service.list(null, "dog", null, new BigDecimal("50"), null, 1, 20))
                .thenReturn(new PageResponse<>(List.of(), 0, 1, 20));
        mvc.perform(get("/services?petType=dog&minPrice=50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void providerServiceListIsPublic() throws Exception {
        mvc.perform(get("/providers/15/services"))
                .andExpect(status().isOk());
        verify(service).list(15L, null, null, null, null, 1, 20);
    }

    @Test
    void publishingRequiresLogin() throws Exception {
        mvc.perform(post("/provider/services").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101));
    }

    @Test
    void createUsesJwtOwnerAndValidatesBody() throws Exception {
        String body = "{\"name\":\"Dog stay\",\"price\":88,\"petType\":\"dog\",\"capacity\":3}";
        mvc.perform(post("/provider/services")
                        .with(jwt().jwt(token -> token.subject("7")))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        verify(service).create(eq(7L), any());
        mvc.perform(post("/provider/services")
                        .with(jwt().jwt(token -> token.subject("7")))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));
    }
}
