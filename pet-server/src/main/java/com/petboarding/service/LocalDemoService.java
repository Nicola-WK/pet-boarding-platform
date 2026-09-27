package com.petboarding.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petboarding.entity.BoardingOrder;
import com.petboarding.entity.BoardingServiceEntity;
import com.petboarding.entity.PaymentRecord;
import com.petboarding.entity.Pet;
import com.petboarding.entity.Provider;
import com.petboarding.entity.Review;
import com.petboarding.entity.User;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.mapper.BoardingOrderMapper;
import com.petboarding.mapper.BoardingServiceMapper;
import com.petboarding.mapper.PaymentRecordMapper;
import com.petboarding.mapper.PetMapper;
import com.petboarding.mapper.ProviderMapper;
import com.petboarding.mapper.ReviewMapper;
import com.petboarding.mapper.UserMapper;
import com.petboarding.security.TokenService;
import com.petboarding.vo.LoginResponse;
import com.petboarding.vo.UserProfile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class LocalDemoService {
    private static final String CUSTOMER_OPENID = "local-demo:customer";
    private static final String PROVIDER_OPENID = "local-demo:provider";
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private final UserMapper users;
    private final ProviderMapper providers;
    private final BoardingServiceMapper services;
    private final PetMapper pets;
    private final BoardingOrderMapper orders;
    private final PaymentRecordMapper payments;
    private final ReviewMapper reviews;
    private final TokenService tokens;

    public LocalDemoService(UserMapper users, ProviderMapper providers, BoardingServiceMapper services,
                            PetMapper pets, BoardingOrderMapper orders, PaymentRecordMapper payments,
                            ReviewMapper reviews, TokenService tokens) {
        this.users = users;
        this.providers = providers;
        this.services = services;
        this.pets = pets;
        this.orders = orders;
        this.payments = payments;
        this.reviews = reviews;
        this.tokens = tokens;
    }

    @Transactional
    public LoginResponse login(String role) {
        if (!"customer".equals(role) && !"provider".equals(role)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        User customer = findOrCreateUser(CUSTOMER_OPENID, "演示宠物主人");
        User merchant = findOrCreateUser(PROVIDER_OPENID, "演示寄养商家");
        if (!Integer.valueOf(1).equals(customer.getStatus())
                || !Integer.valueOf(1).equals(merchant.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        seedCustomerPets(customer);
        seedCatalog(merchant);
        User selected = "provider".equals(role) ? merchant : customer;
        return new LoginResponse(tokens.issue(selected.getId()), "Bearer",
                tokens.expiresInSeconds(), UserProfile.from(selected));
    }

    private User findOrCreateUser(String openid, String nickname) {
        User found = users.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        if (found != null) return found;
        User user = new User();
        user.setOpenid(openid);
        user.setNickname(nickname);
        user.setStatus(1);
        try {
            users.insert(user);
            return user;
        } catch (DuplicateKeyException ex) {
            return users.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        }
    }

    private void seedCustomerPets(User customer) {
        long count = pets.selectCount(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getUserId, customer.getId()).eq(Pet::getDeleted, 0));
        if (count > 0) return;
        seedPet(customer, "豆包", "dog", "柯基", 1, "性格亲人，按时喂食即可");
        seedPet(customer, "奶糖", "cat", "英短", 2, "喜欢安静的空间，适应较慢");
        seedPet(customer, "团团", "other", "垂耳兔", 0, "请保持干草和饮水充足");
    }

    private Pet seedPet(User owner, String name, String type, String breed,
                        int gender, String character) {
        Pet existing = pets.selectOne(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getUserId, owner.getId()).eq(Pet::getName, name)
                .eq(Pet::getDeleted, 0).last("LIMIT 1"));
        if (existing != null) return existing;
        Pet pet = new Pet();
        pet.setUserId(owner.getId());
        pet.setName(name);
        pet.setType(type);
        pet.setBreed(breed);
        pet.setGender(gender);
        pet.setCharacter(character);
        pet.setRemark("本机演示档案，可在小程序中编辑");
        pet.setDeleted(0);
        pets.insert(pet);
        return pet;
    }

    private void seedCatalog(User merchant) {
        Provider first = ensureProvider(merchant, "演示寄养屋", "演示街区 · 家庭寄养体验点",
                "温馨的家庭寄养环境，提供每日活动与喂食记录。此商家仅供本机作品演示。");
        BoardingServiceEntity firstDog = services.selectOne(new LambdaQueryWrapper<BoardingServiceEntity>()
                .eq(BoardingServiceEntity::getProviderId, first.getId())
                .eq(BoardingServiceEntity::getPetType, "dog").last("LIMIT 1"));
        if (firstDog == null) {
            firstDog = seedService(first, "演示狗狗寄养", "dog", "88.00", 5,
                    "家庭式犬类寄养，包含散步、定时喂食与日常陪伴。价格仅为演示数据。");
        } else if ("演示犬猫寄养".equals(firstDog.getName())) {
            firstDog.setName("演示狗狗寄养");
            services.updateById(firstDog);
        }
        seedService(first, "演示猫咪寄养", "cat", "78.00", 4,
                "独立猫咪活动区，提供猫砂清洁和安静休息空间。价格仅为演示数据。");

        User catOwner = findOrCreateUser("local-demo:merchant:cat", "演示猫屋商家");
        Provider cat = ensureProvider(catOwner, "城市猫咪小屋", "演示街区 · 猫咪照护体验点",
                "专注猫咪照护，提供独立空间和每日喂食记录。此商家仅供本机作品演示。");
        BoardingServiceEntity catBasic = seedService(cat, "猫咪安心寄养", "cat", "99.00", 6,
                "标准猫咪照护，含定时喂食与猫砂清理。价格仅为演示数据。");
        seedService(cat, "猫咪独立套间", "cat", "159.00", 3,
                "独立休息空间与更多互动时间。价格仅为演示数据。");

        User dogOwner = findOrCreateUser("local-demo:merchant:dog", "演示犬舍商家");
        Provider dog = ensureProvider(dogOwner, "暖爪家庭寄养", "演示街区 · 犬只陪伴体验点",
                "小规模家庭照护，关注运动与作息。此商家仅供本机作品演示。");
        BoardingServiceEntity dogBasic = seedService(dog, "狗狗活力寄养", "dog", "129.00", 5,
                "每日遛狗与互动，适合精力充沛的狗狗。价格仅为演示数据。");
        seedService(dog, "小宠安静寄养", "other", "69.00", 4,
                "适合兔子等小型宠物的安静照护。价格仅为演示数据。");
        seedService(dog, "狗狗独享照护", "dog", "228.00", 2,
                "少量接待，安排更充足的陪伴时间。价格仅为演示数据。");

        User mixedOwner = findOrCreateUser("local-demo:merchant:mixed", "演示综合商家");
        Provider mixed = ensureProvider(mixedOwner, "森林宠物驿站", "演示街区 · 综合寄养体验点",
                "犬猫和小宠均有独立照护区域。此商家仅供本机作品演示。");
        seedService(mixed, "狗狗舒适寄养", "dog", "168.00", 7,
                "宽敞活动区与固定作息。价格仅为演示数据。");
        seedService(mixed, "猫咪轻松寄养", "cat", "148.00", 5,
                "安静猫区和每日清洁。价格仅为演示数据。");
        seedService(mixed, "小宠专属寄养", "other", "89.00", 4,
                "适合兔子等小宠物的独立空间。价格仅为演示数据。");

        User reviewer = findOrCreateUser("local-demo:reviewer", "演示评价用户");
        Pet reviewDog = seedPet(reviewer, "小栗", "dog", "柴犬", 1, "喜欢散步");
        Pet reviewCat = seedPet(reviewer, "小橘", "cat", "中华田园猫", 0, "喜欢安静");
        seedHistoricalReview("HOME", reviewer, reviewDog, firstDog, first, 5,
                "演示评价：照护安排清楚，能直观展示预约、完成和评价流程。");
        seedHistoricalReview("CAT", reviewer, reviewCat, catBasic, cat, 5,
                "演示评价：猫咪区域安静，适合展示猫咪寄养服务页面。");
        seedHistoricalReview("DOG", reviewer, reviewDog, dogBasic, dog, 4,
                "演示评价：每日活动安排丰富，页面中的评分仅为示例。");
    }

    private Provider ensureProvider(User owner, String name, String address, String description) {
        Provider existing = providers.selectOne(new LambdaQueryWrapper<Provider>()
                .eq(Provider::getOwnerUserId, owner.getId()));
        if (existing != null) return existing;
        Provider provider = new Provider();
        provider.setOwnerUserId(owner.getId());
        provider.setName(name);
        provider.setContactPhone("00000000000");
        provider.setAddress(address);
        provider.setDescription(description);
        provider.setImages("[]");
        provider.setAuditStatus(1);
        provider.setAuditedAt(LocalDateTime.now(BUSINESS_ZONE));
        try {
            providers.insert(provider);
            return provider;
        } catch (DuplicateKeyException ex) {
            return providers.selectOne(new LambdaQueryWrapper<Provider>()
                    .eq(Provider::getOwnerUserId, owner.getId()));
        }
    }

    private BoardingServiceEntity seedService(Provider provider, String name, String petType,
                                               String price, int capacity, String description) {
        BoardingServiceEntity existing = services.selectOne(new LambdaQueryWrapper<BoardingServiceEntity>()
                .eq(BoardingServiceEntity::getProviderId, provider.getId())
                .eq(BoardingServiceEntity::getName, name).last("LIMIT 1"));
        if (existing != null) return existing;
        BoardingServiceEntity service = new BoardingServiceEntity();
        service.setProviderId(provider.getId());
        service.setName(name);
        service.setPrice(new BigDecimal(price));
        service.setPetType(petType);
        service.setCapacity(capacity);
        service.setDescription(description);
        service.setImages("[]");
        service.setStatus(1);
        services.insert(service);
        return service;
    }

    private void seedHistoricalReview(String tag, User reviewer, Pet pet,
                                      BoardingServiceEntity service, Provider provider,
                                      int score, String content) {
        String orderNo = "DEMO-HISTORY-" + tag;
        BoardingOrder order = orders.selectOne(new LambdaQueryWrapper<BoardingOrder>()
                .eq(BoardingOrder::getOrderNo, orderNo));
        if (order == null) {
            int daysAgo = switch (tag) { case "HOME" -> 45; case "CAT" -> 30; default -> 15; };
            LocalDateTime start = LocalDateTime.now(BUSINESS_ZONE).minusDays(daysAgo).toLocalDate().atStartOfDay();
            LocalDateTime end = start.plusDays(2);
            LocalDateTime paid = start.minusDays(4);
            order = new BoardingOrder();
            order.setOrderNo(orderNo);
            order.setUserId(reviewer.getId());
            order.setPetId(pet.getId());
            order.setProviderId(provider.getId());
            order.setServiceId(service.getId());
            order.setStartAt(start);
            order.setEndAt(end);
            order.setBoardingDays(2);
            order.setUnitPrice(service.getPrice());
            order.setTotalAmount(service.getPrice().multiply(BigDecimal.valueOf(2)));
            order.setStatus(3);
            order.setRemark("本机演示历史订单，不涉及真实寄养或支付");
            order.setPaidAt(paid);
            order.setConfirmedAt(paid.plusHours(1));
            order.setCompletedAt(end.plusHours(1));
            orders.insert(order);
        }
        long paymentCount = payments.selectCount(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getOrderId, order.getId()));
        if (paymentCount == 0) {
            PaymentRecord payment = new PaymentRecord();
            payment.setOrderId(order.getId());
            payment.setOutTradeNo("DEMO-HISTORY-PAY-" + tag);
            payment.setAmount(order.getTotalAmount());
            payment.setStatus(1);
            payment.setPaidAt(order.getPaidAt());
            payments.insert(payment);
        }
        long reviewCount = reviews.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, order.getId()));
        if (reviewCount == 0) {
            Review review = new Review();
            review.setOrderId(order.getId());
            review.setUserId(reviewer.getId());
            review.setProviderId(provider.getId());
            review.setServiceId(service.getId());
            review.setScore(score);
            review.setContent(content);
            review.setImages("[]");
            reviews.insert(review);
        }
    }

}
