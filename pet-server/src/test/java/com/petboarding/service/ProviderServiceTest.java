package com.petboarding.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petboarding.config.AdminProperties;
import com.petboarding.dto.ProviderApplicationRequest;
import com.petboarding.dto.ProviderAuditRequest;
import com.petboarding.entity.Provider;
import com.petboarding.entity.User;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.mapper.ProviderMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {
    @BeforeAll
    static void initializeMybatisMetadata() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "test"), Provider.class);
    }

    @Mock ProviderMapper mapper;
    @Mock UserService userService;

    private ProviderService service(String adminOpenids) {
        return new ProviderService(mapper, userService, new ObjectMapper(),
                new AdminProperties(adminOpenids));
    }

    private User activeUser(String openid) {
        User user = new User();
        user.setOpenid(openid);
        user.setStatus(1);
        return user;
    }

    private ProviderApplicationRequest validRequest() {
        return new ProviderApplicationRequest("Happy Paws", "13800138000", "Shanghai",
                null, null, "Small pet boarding", List.of("https://example.com/a.jpg"), null);
    }

    @Test
    void nonAdminCannotReadPendingApplications() {
        when(userService.requireActive(9L)).thenReturn(activeUser("other-openid"));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service("admin-openid").pending(9L, 1, 20));

        assertEquals(ErrorCode.FORBIDDEN, error.errorCode());
        verifyNoInteractions(mapper);
    }

    @Test
    void rejectedApplicationRequiresReason() {
        when(userService.requireActive(9L)).thenReturn(activeUser("admin-openid"));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service("admin-openid").audit(9L, 10L,
                        new ProviderAuditRequest(false, "  ")));

        assertEquals(ErrorCode.VALIDATION_ERROR, error.errorCode());
        verifyNoInteractions(mapper);
    }

    @Test
    void pendingAuditUsesConditionalUpdateToRejectConcurrentDecision() {
        when(userService.requireActive(9L)).thenReturn(activeUser("admin-openid"));
        Provider provider = new Provider();
        provider.setId(10L);
        provider.setAuditStatus(0);
        when(mapper.selectById(10L)).thenReturn(provider);
        when(mapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(0);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service("admin-openid").audit(9L, 10L,
                        new ProviderAuditRequest(true, null)));

        assertEquals(ErrorCode.CONFLICT, error.errorCode());
        verify(mapper).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void duplicateOwnerCannotApplyAgain() {
        when(userService.requireActive(7L)).thenReturn(activeUser("owner-openid"));
        when(mapper.selectOne(any())).thenReturn(new Provider());

        BusinessException error = assertThrows(BusinessException.class,
                () -> service("").apply(7L, validRequest()));

        assertEquals(ErrorCode.CONFLICT, error.errorCode());
    }

    @Test
    void publicDetailNeverReturnsUnapprovedProvider() {
        BusinessException error = assertThrows(BusinessException.class,
                () -> service("").getApproved(10L));

        assertEquals(ErrorCode.NOT_FOUND, error.errorCode());
    }

    @Test
    void locationCoordinatesMustBeProvidedTogether() {
        ProviderApplicationRequest request = new ProviderApplicationRequest(
                "Happy Paws", "13800138000", "Shanghai",
                new java.math.BigDecimal("31.2304000"), null, null, List.of(), null);
        when(userService.requireActive(7L)).thenReturn(activeUser("owner-openid"));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service("").apply(7L, request));

        assertEquals(ErrorCode.VALIDATION_ERROR, error.errorCode());
        verifyNoInteractions(mapper);
    }

    @Test
    void successfulApplicationReloadsDatabaseGeneratedFields() {
        when(userService.requireActive(7L)).thenReturn(activeUser("owner-openid"));
        doAnswer(invocation -> {
            Provider inserted = invocation.getArgument(0);
            inserted.setId(42L);
            return 1;
        }).when(mapper).insert(any(Provider.class));
        Provider stored = new Provider();
        stored.setId(42L);
        stored.setOwnerUserId(7L);
        stored.setName("Happy Paws");
        stored.setImages("[]");
        stored.setAuditStatus(0);
        stored.setCreatedAt(java.time.LocalDateTime.of(2026, 9, 24, 12, 0));
        when(mapper.selectById(42L)).thenReturn(stored);

        var response = service("").apply(7L, validRequest());

        assertEquals(42L, response.id());
        assertEquals(0, response.auditStatus());
        assertEquals(stored.getCreatedAt(), response.createdAt());
    }

    @Test
    void publicListFiltersForApprovedStatus() {
        when(mapper.selectCount(any())).thenReturn(0L);
        when(mapper.selectList(any())).thenReturn(List.of());

        service("").listApproved(null, 1, 20);

        ArgumentCaptor<LambdaQueryWrapper<Provider>> query = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(mapper).selectList(query.capture());
        assertTrue(query.getValue().getSqlSegment().contains("audit_status"));
        assertTrue(query.getValue().getParamNameValuePairs().containsValue(1));
    }}