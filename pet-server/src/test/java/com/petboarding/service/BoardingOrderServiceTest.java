package com.petboarding.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.petboarding.dto.CreateOrderRequest;
import com.petboarding.entity.BoardingOrder;
import com.petboarding.entity.BoardingServiceEntity;
import com.petboarding.entity.Pet;
import com.petboarding.entity.Provider;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.mapper.BoardingOrderMapper;
import com.petboarding.mapper.BoardingServiceMapper;
import com.petboarding.mapper.PetMapper;
import com.petboarding.mapper.ProviderMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardingOrderServiceTest {
    @BeforeAll
    static void metadata() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        TableInfoHelper.initTableInfo(assistant, BoardingOrder.class);
        TableInfoHelper.initTableInfo(assistant, Pet.class);
        TableInfoHelper.initTableInfo(assistant, Provider.class);
    }

    @Mock BoardingOrderMapper mapper;
    @Mock BoardingServiceMapper serviceMapper;
    @Mock PetMapper petMapper;
    @Mock ProviderMapper providerMapper;
    @Mock UserService userService;

    private BoardingOrderService service() {
        return new BoardingOrderService(mapper, serviceMapper, petMapper, providerMapper, userService);
    }

    private CreateOrderRequest request(int days) {
        LocalDate start = LocalDate.now(java.time.ZoneId.of("Asia/Shanghai")).plusDays(1);
        return new CreateOrderRequest(11L, 21L, start, start.plusDays(days), "Care instructions");
    }

    private Pet pet() {
        Pet pet = new Pet();
        pet.setId(11L);
        pet.setUserId(7L);
        pet.setType("dog");
        pet.setDeleted(0);
        return pet;
    }

    private BoardingServiceEntity offered(int capacity) {
        BoardingServiceEntity entity = new BoardingServiceEntity();
        entity.setId(21L);
        entity.setProviderId(31L);
        entity.setPetType("dog");
        entity.setPrice(new BigDecimal("88.00"));
        entity.setCapacity(capacity);
        return entity;
    }

    @Test
    void createsOrderWithPriceSnapshotAndDailyAvailabilityChecks() {
        when(petMapper.lockActiveOwned(11L, 7L)).thenReturn(pet());
        when(mapper.lockService(21L)).thenReturn(21L);
        when(serviceMapper.lockVisibleById(21L)).thenReturn(offered(2));
        when(mapper.findPetReservations(eq(11L), any(), any(), any())).thenReturn(List.of());
        when(mapper.findReservedIds(eq(21L), any(), any(), any(), eq(2))).thenReturn(List.of());
        doAnswer(invocation -> {
            BoardingOrder order = invocation.getArgument(0);
            order.setId(41L);
            return 1;
        }).when(mapper).insert(any(BoardingOrder.class));
        BoardingOrder stored = new BoardingOrder();
        stored.setId(41L);
        stored.setUnitPrice(new BigDecimal("88.00"));
        stored.setTotalAmount(new BigDecimal("176.00"));
        stored.setStatus(0);
        when(mapper.selectById(41L)).thenReturn(stored);

        var result = service().create(7L, request(2));

        ArgumentCaptor<BoardingOrder> captured = ArgumentCaptor.forClass(BoardingOrder.class);
        verify(mapper).insert(captured.capture());
        assertEquals(7L, captured.getValue().getUserId());
        assertEquals(31L, captured.getValue().getProviderId());
        assertEquals(new BigDecimal("176.00"), captured.getValue().getTotalAmount());
        assertEquals(2, captured.getValue().getBoardingDays());
        assertTrue(captured.getValue().getOrderNo().startsWith("PB"));
        assertEquals(0, result.status());
        org.mockito.Mockito.verify(mapper, org.mockito.Mockito.times(2))
                .findReservedIds(eq(21L), any(), any(), any(), eq(2));
    }

    @Test
    void fullDayRejectsBookingBeforeInsert() {
        when(petMapper.lockActiveOwned(11L, 7L)).thenReturn(pet());
        when(mapper.lockService(21L)).thenReturn(21L);
        when(serviceMapper.lockVisibleById(21L)).thenReturn(offered(1));
        when(mapper.findPetReservations(eq(11L), any(), any(), any())).thenReturn(List.of());
        when(mapper.findReservedIds(eq(21L), any(), any(), any(), eq(1)))
                .thenReturn(List.of(99L));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service().create(7L, request(1)));

        assertEquals(ErrorCode.CONFLICT, error.errorCode());
        org.mockito.Mockito.verify(mapper, org.mockito.Mockito.never()).insert(any(BoardingOrder.class));
    }

    @Test
    void cannotBookAnotherUsersPet() {
        BusinessException error = assertThrows(BusinessException.class,
                () -> service().create(7L, request(1)));
        assertEquals(ErrorCode.NOT_FOUND, error.errorCode());
        verifyNoInteractions(serviceMapper);
    }

    @Test
    void invalidDateRangeIsRejectedBeforeDatabaseRead() {
        BusinessException error = assertThrows(BusinessException.class,
                () -> service().create(7L, request(0)));
        assertEquals(ErrorCode.VALIDATION_ERROR, error.errorCode());
        verifyNoInteractions(petMapper, mapper);
    }

    @Test
    void canceledPaidOrderCannotChangeState() {
        BoardingOrder order = new BoardingOrder();
        order.setId(41L);
        order.setUserId(7L);
        order.setStatus(1);
        when(mapper.selectOne(any())).thenReturn(order);
        when(mapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(0);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service().cancel(7L, 41L));
        assertEquals(ErrorCode.CONFLICT, error.errorCode());
    }

    @Test
    void unpaidOrderCannotBeConfirmedByProvider() {
        Provider provider = new Provider();
        provider.setId(31L);
        when(providerMapper.selectOne(any())).thenReturn(provider);
        BoardingOrder order = new BoardingOrder();
        order.setId(41L);
        order.setProviderId(31L);
        order.setStatus(0);
        order.setEndAt(LocalDateTime.now().plusDays(2));
        when(mapper.selectOne(any())).thenReturn(order);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service().confirm(9L, 41L));
        assertEquals(ErrorCode.CONFLICT, error.errorCode());
        org.mockito.Mockito.verify(mapper, org.mockito.Mockito.never())
                .update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void providerCannotReadAnotherProvidersOrder() {
        Provider provider = new Provider();
        provider.setId(31L);
        when(providerMapper.selectOne(any())).thenReturn(provider);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service().getProviderOrder(9L, 41L));

        assertEquals(ErrorCode.NOT_FOUND, error.errorCode());
        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BoardingOrder>> query =
                ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class);
        verify(mapper).selectOne(query.capture());
        assertTrue(query.getValue().getSqlSegment().contains("provider_id"));
        assertTrue(query.getValue().getParamNameValuePairs().containsValue(31L));
    }

    @Test
    void samePetCannotHaveOverlappingOrders() {
        when(petMapper.lockActiveOwned(11L, 7L)).thenReturn(pet());
        when(mapper.lockService(21L)).thenReturn(21L);
        when(serviceMapper.lockVisibleById(21L)).thenReturn(offered(3));
        when(mapper.findPetReservations(eq(11L), any(), any(), any()))
                .thenReturn(List.of(99L));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service().create(7L, request(1)));

        assertEquals(ErrorCode.CONFLICT, error.errorCode());
        org.mockito.Mockito.verify(mapper, org.mockito.Mockito.never())
                .findReservedIds(anyLong(), any(), any(), any(), anyInt());
    }
}
