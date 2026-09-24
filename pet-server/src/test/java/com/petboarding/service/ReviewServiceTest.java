package com.petboarding.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petboarding.dto.ReviewRequest;
import com.petboarding.entity.BoardingOrder;
import com.petboarding.entity.Review;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.mapper.BoardingOrderMapper;
import com.petboarding.mapper.ReviewMapper;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @BeforeAll
    static void metadata() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        TableInfoHelper.initTableInfo(assistant, Review.class);
    }

    @Mock ReviewMapper mapper;
    @Mock BoardingOrderMapper orderMapper;
    @Mock UserService userService;
    @Mock ProviderService providerService;
    @Mock BoardingServiceService boardingServiceService;

    private ReviewService service() {
        return new ReviewService(mapper, orderMapper, userService, providerService,
                boardingServiceService, new ObjectMapper());
    }

    private BoardingOrder order(long ownerId, int status) {
        BoardingOrder order = new BoardingOrder();
        order.setId(41L);
        order.setUserId(ownerId);
        order.setProviderId(31L);
        order.setServiceId(21L);
        order.setStatus(status);
        return order;
    }

    private ReviewRequest request() {
        return new ReviewRequest(5, "Great care", List.of("https://example.com/pet.jpg"));
    }

    @Test
    void anotherUserCannotReviewOrder() {
        when(orderMapper.lockOrder(41L)).thenReturn(order(8L, 3));
        BusinessException error = assertThrows(BusinessException.class,
                () -> service().create(7L, 41L, request()));
        assertEquals(ErrorCode.NOT_FOUND, error.errorCode());
        verifyNoInteractions(mapper);
    }

    @Test
    void unfinishedOrderCannotBeReviewed() {
        when(orderMapper.lockOrder(41L)).thenReturn(order(7L, 2));
        BusinessException error = assertThrows(BusinessException.class,
                () -> service().create(7L, 41L, request()));
        assertEquals(ErrorCode.CONFLICT, error.errorCode());
        verifyNoInteractions(mapper);
    }

    @Test
    void duplicateReviewIsRejected() {
        when(orderMapper.lockOrder(41L)).thenReturn(order(7L, 3));
        when(mapper.lockByOrderId(41L)).thenReturn(new Review());
        BusinessException error = assertThrows(BusinessException.class,
                () -> service().create(7L, 41L, request()));
        assertEquals(ErrorCode.CONFLICT, error.errorCode());
        org.mockito.Mockito.verify(mapper, org.mockito.Mockito.never()).insert(any(Review.class));
    }

    @Test
    void reviewCopiesProviderAndServiceFromCompletedOrder() {
        when(orderMapper.lockOrder(41L)).thenReturn(order(7L, 3));
        doAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(51L);
            return 1;
        }).when(mapper).insert(any(Review.class));
        Review stored = new Review();
        stored.setId(51L);
        stored.setProviderId(31L);
        stored.setServiceId(21L);
        stored.setScore(5);
        stored.setImages("[\"https://example.com/pet.jpg\"]");
        when(mapper.selectById(51L)).thenReturn(stored);

        var result = service().create(7L, 41L, request());

        ArgumentCaptor<Review> captured = ArgumentCaptor.forClass(Review.class);
        verify(mapper).insert(captured.capture());
        assertEquals(31L, captured.getValue().getProviderId());
        assertEquals(21L, captured.getValue().getServiceId());
        assertEquals(7L, captured.getValue().getUserId());
        assertEquals(1, result.images().size());
    }

    @Test
    void publicProviderListChecksApprovalAndFiltersProvider() {
        when(mapper.selectCount(any())).thenReturn(0L);
        when(mapper.selectList(any())).thenReturn(List.of());

        service().listByProvider(31L, 1, 20);

        verify(providerService).getApproved(31L);
        ArgumentCaptor<LambdaQueryWrapper<Review>> query = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(mapper).selectList(query.capture());
        assertTrue(query.getValue().getSqlSegment().contains("provider_id"));
        assertTrue(query.getValue().getParamNameValuePairs().containsValue(31L));
    }

    @Test
    void userCannotReadAnotherUsersReview() {
        BusinessException error = assertThrows(BusinessException.class,
                () -> service().getMine(7L, 41L));
        assertEquals(ErrorCode.NOT_FOUND, error.errorCode());
    }
}
