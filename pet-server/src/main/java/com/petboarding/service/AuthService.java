package com.petboarding.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petboarding.entity.User;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.mapper.UserMapper;
import com.petboarding.security.TokenService;
import com.petboarding.vo.LoginResponse;
import com.petboarding.vo.UserProfile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final WechatCodeClient wechatClient;
    private final UserMapper userMapper;
    private final TokenService tokenService;

    public AuthService(WechatCodeClient wechatClient, UserMapper userMapper, TokenService tokenService) {
        this.wechatClient = wechatClient;
        this.userMapper = userMapper;
        this.tokenService = tokenService;
    }

    @Transactional
    public LoginResponse login(String code) {
        WechatCodeClient.WechatIdentity identity = wechatClient.exchange(code);
        User user = findByOpenid(identity.openid());
        if (user == null) {
            User created = new User();
            created.setOpenid(identity.openid());
            created.setUnionid(identity.unionid());
            created.setNickname("");
            created.setStatus(1);
            try {
                userMapper.insert(created);
                user = created;
            } catch (DuplicateKeyException ex) {
                user = findByOpenid(identity.openid());
                if (user == null) {
                    throw new BusinessException(ErrorCode.CONFLICT);
                }
            }
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return new LoginResponse(tokenService.issue(user.getId()), "Bearer",
                tokenService.expiresInSeconds(), UserProfile.from(user));
    }

    private User findByOpenid(String openid) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
    }
}
