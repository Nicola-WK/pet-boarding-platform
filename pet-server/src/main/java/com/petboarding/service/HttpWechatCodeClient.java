package com.petboarding.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petboarding.config.WechatProperties;
import com.petboarding.exception.BusinessException;
import com.petboarding.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpWechatCodeClient implements WechatCodeClient {
    private static final Logger log = LoggerFactory.getLogger(HttpWechatCodeClient.class);
    private final RestClient client;
    private final WechatProperties properties;

    public HttpWechatCodeClient(WechatProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.client = RestClient.builder()
                .baseUrl("https://api.weixin.qq.com")
                .requestFactory(factory)
                .build();
    }

    @Override
    public WechatIdentity exchange(String code) {
        if (properties.appId() == null || properties.appId().isBlank()
                || properties.secret() == null || properties.secret().isBlank()) {
            throw new IllegalStateException("WECHAT_APP_ID and WECHAT_APP_SECRET must be set");
        }
        try {
            WechatReply reply = client.get()
                    .uri(uri -> uri.path("/sns/jscode2session")
                            .queryParam("appid", properties.appId())
                            .queryParam("secret", properties.secret())
                            .queryParam("js_code", code)
                            .queryParam("grant_type", "authorization_code")
                            .build())
                    .retrieve()
                    .body(WechatReply.class);
            if (reply == null) {
                throw new BusinessException(ErrorCode.WECHAT_UPSTREAM_ERROR);
            }
            if (reply.errcode() != null && reply.errcode() != 0) {
                log.warn("WeChat code2Session rejected code, errcode={}", reply.errcode());
                throw new BusinessException(ErrorCode.WECHAT_LOGIN_FAILED);
            }
            if (reply.openid() == null || reply.openid().isBlank()) {
                throw new BusinessException(ErrorCode.WECHAT_UPSTREAM_ERROR);
            }
            return new WechatIdentity(reply.openid(), reply.unionid());
        } catch (RestClientException ex) {
            log.warn("WeChat code2Session request failed: {}", ex.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.WECHAT_UPSTREAM_ERROR);
        }
    }

    private record WechatReply(
            @JsonProperty("openid") String openid,
            @JsonProperty("unionid") String unionid,
            @JsonProperty("errcode") Integer errcode) {
    }
}
