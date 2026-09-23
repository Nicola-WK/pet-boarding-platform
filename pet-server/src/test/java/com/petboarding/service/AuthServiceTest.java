package com.petboarding.service;

import com.petboarding.entity.User;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import com.petboarding.mapper.UserMapper;
import com.petboarding.security.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock WechatCodeClient wechatClient;
    @Mock UserMapper userMapper;
    @Mock TokenService tokenService;
    @InjectMocks AuthService service;

    @Test
    void existingOpenidReusesUserAndIssuesToken() {
        User user = new User();
        user.setId(42L);
        user.setOpenid("openid-1");
        user.setNickname("pet owner");
        user.setStatus(1);
        when(wechatClient.exchange("valid-code"))
                .thenReturn(new WechatCodeClient.WechatIdentity("openid-1", null));
        when(userMapper.selectOne(any())).thenReturn(user);
        when(tokenService.issue(42L)).thenReturn("signed-token");
        when(tokenService.expiresInSeconds()).thenReturn(3600L);

        var result = service.login("valid-code");

        assertEquals("signed-token", result.token());
        assertEquals(42L, result.user().id());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void newOpenidCreatesUserBeforeIssuingToken() {
        when(wechatClient.exchange("new-code"))
                .thenReturn(new WechatCodeClient.WechatIdentity("new-openid", null));
        when(userMapper.selectOne(any())).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User created = invocation.getArgument(0);
            created.setId(101L);
            return 1;
        });
        when(tokenService.issue(101L)).thenReturn("new-token");

        var result = service.login("new-code");

        assertEquals(101L, result.user().id());
        assertEquals("new-token", result.token());
        verify(userMapper).insert(any(User.class));
    }
    @Test
    void disabledUserCannotLogIn() {
        User user = new User();
        user.setId(42L);
        user.setStatus(0);
        when(wechatClient.exchange("valid-code"))
                .thenReturn(new WechatCodeClient.WechatIdentity("openid-1", null));
        when(userMapper.selectOne(any())).thenReturn(user);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.login("valid-code"));

        assertEquals(ErrorCode.FORBIDDEN, error.errorCode());
        verify(tokenService, never()).issue(42L);
    }
}
