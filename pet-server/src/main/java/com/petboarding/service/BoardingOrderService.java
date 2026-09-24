package com.petboarding.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import com.petboarding.vo.BoardingOrderResponse;
import com.petboarding.vo.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class BoardingOrderService {
    private static final int UNPAID = 0;
    private static final int PENDING_CONFIRMATION = 1;
    private static final int BOARDING = 2;
    private static final int COMPLETED = 3;
    private static final int CANCELED = 4;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_DAYS = 90;
    private static final Duration PAYMENT_WINDOW = Duration.ofMinutes(15);
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    private final BoardingOrderMapper mapper;
    private final BoardingServiceMapper serviceMapper;
    private final PetMapper petMapper;
    private final ProviderMapper providerMapper;
    private final UserService userService;

    public BoardingOrderService(BoardingOrderMapper mapper, BoardingServiceMapper serviceMapper,
                                PetMapper petMapper, ProviderMapper providerMapper,
                                UserService userService) {
        this.mapper = mapper;
        this.serviceMapper = serviceMapper;
        this.petMapper = petMapper;
        this.providerMapper = providerMapper;
        this.userService = userService;
    }

    @Transactional
    public BoardingOrderResponse create(long userId, CreateOrderRequest request) {
        userService.requireActive(userId);
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate());
        if (request.startDate().isBefore(today) || days < 1 || days > MAX_DAYS) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Pet pet = petMapper.lockActiveOwned(request.petId(), userId);
        if (pet == null) throw new BusinessException(ErrorCode.NOT_FOUND);

        // All creators lock the same service row before counting overlapping reservations.
        if (mapper.lockService(request.serviceId()) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        BoardingServiceEntity offered = serviceMapper.lockVisibleById(request.serviceId());
        if (offered == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        if (!offered.getPetType().equals(pet.getType())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        LocalDateTime startAt = request.startDate().atStartOfDay();
        LocalDateTime endAt = request.endDate().atStartOfDay();
        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);
        if (!mapper.findPetReservations(pet.getId(), startAt, endAt, now).isEmpty()) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        for (LocalDate day = request.startDate(); day.isBefore(request.endDate()); day = day.plusDays(1)) {
            if (mapper.findReservedIds(offered.getId(), day.atStartOfDay(),
                    day.plusDays(1).atStartOfDay(), now, offered.getCapacity()).size()
                    >= offered.getCapacity()) {
                throw new BusinessException(ErrorCode.CONFLICT);
            }
        }
        BigDecimal total = offered.getPrice().multiply(BigDecimal.valueOf(days));
        if (total.precision() - total.scale() > 8) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        BoardingOrder order = new BoardingOrder();
        order.setOrderNo("PB" + UUID.randomUUID().toString().replace("-", ""));
        order.setUserId(userId);
        order.setPetId(pet.getId());
        order.setProviderId(offered.getProviderId());
        order.setServiceId(offered.getId());
        order.setStartAt(startAt);
        order.setEndAt(endAt);
        order.setBoardingDays((int) days);
        order.setUnitPrice(offered.getPrice());
        order.setTotalAmount(total);
        order.setStatus(UNPAID);
        order.setRemark(request.remark());
        order.setExpiresAt(now.plus(PAYMENT_WINDOW));
        mapper.insert(order);
        return BoardingOrderResponse.from(mapper.selectById(order.getId()));
    }

    @Transactional(readOnly = true)
    public PageResponse<BoardingOrderResponse> mine(long userId, Integer status, int page, int size) {
        userService.requireActive(userId);
        return page(new LambdaQueryWrapper<BoardingOrder>().eq(BoardingOrder::getUserId, userId),
                status, page, size);
    }

    @Transactional(readOnly = true)
    public BoardingOrderResponse getMine(long userId, long id) {
        userService.requireActive(userId);
        BoardingOrder order = mapper.selectOne(new LambdaQueryWrapper<BoardingOrder>()
                .eq(BoardingOrder::getId, id).eq(BoardingOrder::getUserId, userId));
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        return BoardingOrderResponse.from(order);
    }

    @Transactional
    public BoardingOrderResponse cancel(long userId, long id) {
        getMine(userId, id);
        int updated = mapper.update(null, new LambdaUpdateWrapper<BoardingOrder>()
                .set(BoardingOrder::getStatus, CANCELED)
                .set(BoardingOrder::getCanceledAt, LocalDateTime.now(BUSINESS_ZONE))
                .eq(BoardingOrder::getId, id)
                .eq(BoardingOrder::getUserId, userId)
                .eq(BoardingOrder::getStatus, UNPAID));
        if (updated != 1) throw new BusinessException(ErrorCode.CONFLICT);
        return getMine(userId, id);
    }

    @Transactional(readOnly = true)
    public PageResponse<BoardingOrderResponse> providerOrders(long userId, Integer status,
                                                              int page, int size) {
        Provider provider = requireProvider(userId);
        return page(new LambdaQueryWrapper<BoardingOrder>()
                .eq(BoardingOrder::getProviderId, provider.getId()), status, page, size);
    }

    @Transactional(readOnly = true)
    public BoardingOrderResponse getProviderOrder(long userId, long id) {
        Provider provider = requireProvider(userId);
        BoardingOrder order = mapper.selectOne(new LambdaQueryWrapper<BoardingOrder>()
                .eq(BoardingOrder::getId, id)
                .eq(BoardingOrder::getProviderId, provider.getId()));
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        return BoardingOrderResponse.from(order);
    }

    @Transactional
    public BoardingOrderResponse confirm(long userId, long id) {
        BoardingOrder order = requireProviderOrder(userId, id);
        if (!Integer.valueOf(PENDING_CONFIRMATION).equals(order.getStatus())
                || order.getConfirmedAt() != null
                || !LocalDateTime.now(BUSINESS_ZONE).isBefore(order.getEndAt())) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        int updated = mapper.update(null, new LambdaUpdateWrapper<BoardingOrder>()
                .set(BoardingOrder::getConfirmedAt, LocalDateTime.now(BUSINESS_ZONE))
                .eq(BoardingOrder::getId, id)
                .eq(BoardingOrder::getProviderId, order.getProviderId())
                .eq(BoardingOrder::getStatus, PENDING_CONFIRMATION)
                .isNull(BoardingOrder::getConfirmedAt));
        if (updated != 1) throw new BusinessException(ErrorCode.CONFLICT);
        return getProviderOrder(userId, id);
    }

    @Transactional
    public BoardingOrderResponse start(long userId, long id) {
        BoardingOrder order = requireProviderOrder(userId, id);
        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);
        if (!Integer.valueOf(PENDING_CONFIRMATION).equals(order.getStatus())
                || order.getConfirmedAt() == null || now.isBefore(order.getStartAt())
                || !now.isBefore(order.getEndAt())) throw new BusinessException(ErrorCode.CONFLICT);
        int updated = mapper.update(null, new LambdaUpdateWrapper<BoardingOrder>()
                .set(BoardingOrder::getStatus, BOARDING)
                .eq(BoardingOrder::getId, id)
                .eq(BoardingOrder::getProviderId, order.getProviderId())
                .eq(BoardingOrder::getStatus, PENDING_CONFIRMATION)
                .isNotNull(BoardingOrder::getConfirmedAt));
        if (updated != 1) throw new BusinessException(ErrorCode.CONFLICT);
        return getProviderOrder(userId, id);
    }

    @Transactional
    public BoardingOrderResponse complete(long userId, long id) {
        BoardingOrder order = requireProviderOrder(userId, id);
        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);
        if (!Integer.valueOf(BOARDING).equals(order.getStatus())
                || now.isBefore(order.getEndAt())) throw new BusinessException(ErrorCode.CONFLICT);
        int updated = mapper.update(null, new LambdaUpdateWrapper<BoardingOrder>()
                .set(BoardingOrder::getStatus, COMPLETED)
                .set(BoardingOrder::getCompletedAt, now)
                .eq(BoardingOrder::getId, id)
                .eq(BoardingOrder::getProviderId, order.getProviderId())
                .eq(BoardingOrder::getStatus, BOARDING));
        if (updated != 1) throw new BusinessException(ErrorCode.CONFLICT);
        return getProviderOrder(userId, id);
    }

    private BoardingOrder requireProviderOrder(long userId, long id) {
        Provider provider = requireProvider(userId);
        BoardingOrder order = mapper.selectOne(new LambdaQueryWrapper<BoardingOrder>()
                .eq(BoardingOrder::getId, id)
                .eq(BoardingOrder::getProviderId, provider.getId()));
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        return order;
    }

    private Provider requireProvider(long userId) {
        userService.requireActive(userId);
        Provider provider = providerMapper.selectOne(new LambdaQueryWrapper<Provider>()
                .eq(Provider::getOwnerUserId, userId));
        if (provider == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        return provider;
    }

    private PageResponse<BoardingOrderResponse> page(LambdaQueryWrapper<BoardingOrder> query,
                                                     Integer status, int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE
                || status != null && (status < UNPAID || status > CANCELED)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (status != null) query.eq(BoardingOrder::getStatus, status);
        long total = mapper.selectCount(query);
        long offset = ((long) page - 1) * size;
        List<BoardingOrderResponse> items = mapper.selectList(query
                .orderByDesc(BoardingOrder::getCreatedAt, BoardingOrder::getId)
                .last("LIMIT " + size + " OFFSET " + offset))
                .stream().map(BoardingOrderResponse::from).toList();
        return new PageResponse<>(items, total, page, size);
    }
}
