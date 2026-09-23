package com.petboarding.service;

public interface WechatCodeClient {
    WechatIdentity exchange(String code);

    record WechatIdentity(String openid, String unionid) {
    }
}
