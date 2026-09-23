package com.petboarding.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.petboarding.dto.PetRequest;
import com.petboarding.entity.Pet;
import com.petboarding.entity.User;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.mapper.PetMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {
    @BeforeAll
    static void initializeMybatisMetadata() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "test"), Pet.class);
    }

    @Mock PetMapper petMapper;
    @Mock UserService userService;
    @InjectMocks PetService service;

    @Test
    void updateReturnsNotFoundWhenOwnedActivePetDoesNotMatch() {
        when(userService.requireActive(7L)).thenReturn(new User());
        when(petMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(0);
        PetRequest request = new PetRequest("Mimi", null, "cat", null,
                2, null, null, null, null, null, null);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.update(7L, 99L, request));

        assertEquals(ErrorCode.NOT_FOUND, error.errorCode());
        verify(userService).requireActive(7L);
        ArgumentCaptor<LambdaUpdateWrapper<Pet>> condition = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(petMapper).update(isNull(), condition.capture());
        assertTrue(condition.getValue().getSqlSegment().contains("user_id"));
        assertTrue(condition.getValue().getParamNameValuePairs().containsValue(7L));
        assertTrue(condition.getValue().getParamNameValuePairs().containsValue(99L));
    }

    @Test
    void deleteReturnsNotFoundInsteadOfChangingAnotherUsersPet() {
        when(userService.requireActive(7L)).thenReturn(new User());
        when(petMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(0);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.delete(7L, 99L));

        assertEquals(ErrorCode.NOT_FOUND, error.errorCode());
        verify(userService).requireActive(7L);
    }
}
