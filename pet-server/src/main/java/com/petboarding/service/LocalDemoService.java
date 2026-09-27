package com.petboarding.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petboarding.entity.BoardingServiceEntity;
import com.petboarding.entity.Provider;
import com.petboarding.entity.User;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.mapper.BoardingServiceMapper;
import com.petboarding.mapper.ProviderMapper;
import com.petboarding.mapper.UserMapper;
import com.petboarding.security.TokenService;
import com.petboarding.vo.LoginResponse;
import com.petboarding.vo.UserProfile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class LocalDemoService {
    private static final String CUSTOMER_OPENID = "local-demo:customer";
    private static final String PROVIDER_OPENID = "local-demo:provider";
    private final UserMapper users;
    private final ProviderMapper providers;
    private final BoardingServiceMapper services;
    private final TokenService tokens;

    public LocalDemoService(UserMapper users, ProviderMapper providers,
                            BoardingServiceMapper services, TokenService tokens) {
        this.users = users;
        this.providers = providers;
        this.services = services;
        this.tokens = tokens;
    }

    @Transactional
    public LoginResponse login(String role) {
        if (!"customer".equals(role) && !"provider".equals(role)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        User customer = findOrCreateUser(CUSTOMER_OPENID, "演示宠物主人");
        User merchant = findOrCreateUser(PROVIDER_OPENID, "演示寄养商家");
        seedMerchant(merchant);
        User selected = "provider".equals(role) ? merchant : customer;
        if (!Integer.valueOf(1).equals(selected.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
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

    private void seedMerchant(User merchant) {
        Provider provider = providers.selectOne(new LambdaQueryWrapper<Provider>()
                .eq(Provider::getOwnerUserId, merchant.getId()));
        if (provider == null) {
            provider = new Provider();
            provider.setOwnerUserId(merchant.getId());
            provider.setName("演示寄养屋");
            provider.setContactPhone("00000000000");
            provider.setAddress("本地演示数据，无实际经营地址");
            provider.setDescription("用于作品集体验的示例商家，可通过商家中心管理服务。请勿作为真实寄养信息使用。");
            provider.setImages("[]");
            provider.setAuditStatus(1);
            provider.setAuditedAt(LocalDateTime.now());
            try {
                providers.insert(provider);
            } catch (DuplicateKeyException ex) {
                provider = providers.selectOne(new LambdaQueryWrapper<Provider>()
                        .eq(Provider::getOwnerUserId, merchant.getId()));
            }
        }
        long count = services.selectCount(new LambdaQueryWrapper<BoardingServiceEntity>()
                .eq(BoardingServiceEntity::getProviderId, provider.getId()));
        if (count == 0) {
            BoardingServiceEntity service = new BoardingServiceEntity();
            service.setProviderId(provider.getId());
            service.setName("演示犬猫寄养");
            service.setPrice(new BigDecimal("88.00"));
            service.setPetType("dog");
            service.setCapacity(5);
            service.setDescription("本地演示服务，价格和预约仅用于体验，不涉及真实资金或寄养。");
            service.setImages("[]");
            service.setStatus(1);
            services.insert(service);
        }
    }
}

