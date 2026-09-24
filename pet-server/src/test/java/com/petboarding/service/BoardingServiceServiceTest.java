package com.petboarding.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petboarding.dto.BoardingServiceRequest;
import com.petboarding.entity.BoardingServiceEntity;
import com.petboarding.entity.Provider;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.mapper.BoardingServiceMapper;
import com.petboarding.mapper.ProviderMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardingServiceServiceTest {
    @BeforeAll
    static void metadata() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        TableInfoHelper.initTableInfo(assistant, BoardingServiceEntity.class);
        TableInfoHelper.initTableInfo(assistant, Provider.class);
    }

    @Mock BoardingServiceMapper mapper;
    @Mock ProviderMapper providerMapper;
    @Mock UserService userService;
    @Mock ProviderService providerService;

    private BoardingServiceService service() {
        return new BoardingServiceService(mapper, providerMapper, userService,
                providerService, new ObjectMapper());
    }

    private Provider provider(int status) {
        Provider p = new Provider();
        p.setId(15L);
        p.setOwnerUserId(7L);
        p.setAuditStatus(status);
        return p;
    }

    private BoardingServiceRequest request() {
        return new BoardingServiceRequest("Dog stay", new BigDecimal("88.00"),
                "dog", 3, "Daily care", List.of("https://example.com/a.jpg"));
    }

    @Test
    void pendingProviderCannotPublish() {
        when(providerMapper.selectOne(any())).thenReturn(provider(0));
        BusinessException error = assertThrows(BusinessException.class,
                () -> service().create(7L, request()));
        assertEquals(ErrorCode.FORBIDDEN, error.errorCode());
        verifyNoInteractions(mapper);
    }

    @Test
    void createDerivesProviderIdFromAuthenticatedOwner() {
        when(providerMapper.selectOne(any())).thenReturn(provider(1));
        org.mockito.Mockito.doAnswer(invocation -> {
            BoardingServiceEntity entity = invocation.getArgument(0);
            entity.setId(21L);
            return 1;
        }).when(mapper).insert(any(BoardingServiceEntity.class));
        BoardingServiceEntity stored = new BoardingServiceEntity();
        stored.setId(21L);
        stored.setProviderId(15L);
        stored.setImages("[\"https://example.com/a.jpg\"]");
        stored.setStatus(1);
        when(mapper.selectById(21L)).thenReturn(stored);

        var result = service().create(7L, request());

        ArgumentCaptor<BoardingServiceEntity> captured = ArgumentCaptor.forClass(BoardingServiceEntity.class);
        verify(mapper).insert(captured.capture());
        assertEquals(15L, captured.getValue().getProviderId());
        assertEquals(1, captured.getValue().getStatus());
        assertEquals(21L, result.id());
        assertEquals(1, result.images().size());
    }

    @Test
    void anotherProvidersServiceCannotBeEdited() {
        when(providerMapper.selectOne(any())).thenReturn(provider(1));
        BusinessException error = assertThrows(BusinessException.class,
                () -> service().update(7L, 99L, request()));
        assertEquals(ErrorCode.NOT_FOUND, error.errorCode());
        ArgumentCaptor<LambdaQueryWrapper<BoardingServiceEntity>> query =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(mapper).selectOne(query.capture());
        assertTrue(query.getValue().getSqlSegment().contains("provider_id"));
        assertTrue(query.getValue().getParamNameValuePairs().containsValue(15L));
    }

    @Test
    void publicDetailOnlyUsesVisibleJoinQuery() {
        BusinessException error = assertThrows(BusinessException.class, () -> service().get(99L));
        assertEquals(ErrorCode.NOT_FOUND, error.errorCode());
        verify(mapper).findVisibleById(99L);
    }

    @Test
    void invalidPriceRangeFailsBeforeQuery() {
        BusinessException error = assertThrows(BusinessException.class,
                () -> service().list(null, null, null,
                        new BigDecimal("100"), new BigDecimal("50"), 1, 20));
        assertEquals(ErrorCode.VALIDATION_ERROR, error.errorCode());
        verifyNoInteractions(mapper);
    }
    @Test
    void ownerCanReadInactiveServiceForEditing() {
        when(providerMapper.selectOne(any())).thenReturn(provider(1));
        BoardingServiceEntity inactive = new BoardingServiceEntity();
        inactive.setId(21L);
        inactive.setProviderId(15L);
        inactive.setStatus(0);
        inactive.setImages("[]");
        when(mapper.selectOne(any())).thenReturn(inactive);
        assertEquals(0, service().getMine(7L, 21L).status());
    }
}
