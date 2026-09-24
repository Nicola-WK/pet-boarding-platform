package com.petboarding.controller;

import com.petboarding.config.JwtProperties;
import com.petboarding.mapper.BoardingServiceMapper;
import com.petboarding.mapper.PetMapper;
import com.petboarding.mapper.ProviderMapper;
import com.petboarding.mapper.UserMapper;
import com.petboarding.security.SecurityConfig;
import com.petboarding.service.ProviderService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ProviderController.class, ProviderOwnerController.class, ProviderAdminController.class})
@Import(SecurityConfig.class)
@EnableConfigurationProperties(JwtProperties.class)
@TestPropertySource(properties = {
        "app.jwt.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.jwt.issuer=https://pet.example.test",
        "app.jwt.ttl=PT1H"
})
class ProviderWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean ProviderService providerService;
    @MockitoBean ProviderMapper providerMapper;
    @MockitoBean PetMapper petMapper;
    @MockitoBean BoardingServiceMapper boardingServiceMapper;
    @MockitoBean UserMapper userMapper;

    @Test
    void approvedProviderListIsPublic() throws Exception {
        when(providerService.listApproved(null, 1, 20))
                .thenReturn(new PageResponse<>(List.of(), 0, 1, 20));

        mvc.perform(get("/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void approvedProviderDetailIsPublic() throws Exception {
        mvc.perform(get("/providers/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        verify(providerService).getApproved(10L);
    }

    @Test
    void applicationRequiresAuthentication() throws Exception {
        mvc.perform(post("/provider/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Happy Paws\",\"contactPhone\":\"13800138000\",\"address\":\"Shanghai\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101));
    }

    @Test
    void ownerIdComesFromJwtSubject() throws Exception {
        String body = "{\"name\":\"Happy Paws\",\"contactPhone\":\"13800138000\",\"address\":\"Shanghai\"}";

        mvc.perform(post("/provider/apply")
                        .with(jwt().jwt(token -> token.subject("7")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(providerService).apply(org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void invalidApplicationUsesUnifiedValidationError() throws Exception {
        mvc.perform(post("/provider/apply")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"contactPhone\":\"123\",\"address\":\"Shanghai\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));
    }

    @Test
    void adminAuditRequiresAuthentication() throws Exception {
        mvc.perform(post("/admin/providers/10/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true}"))
                .andExpect(status().isUnauthorized());
    }
}