package com.petboarding.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_ERROR(40001, HttpStatus.BAD_REQUEST, "请求参数无效"),
    WECHAT_LOGIN_FAILED(40029, HttpStatus.BAD_REQUEST, "微信登录凭证无效"),
    UNAUTHORIZED(40101, HttpStatus.UNAUTHORIZED, "请先登录"),
    FORBIDDEN(40301, HttpStatus.FORBIDDEN, "没有操作权限"),
    NOT_FOUND(40401, HttpStatus.NOT_FOUND, "资源不存在"),
    CONFLICT(40901, HttpStatus.CONFLICT, "数据冲突"),
    WECHAT_UPSTREAM_ERROR(50201, HttpStatus.BAD_GATEWAY, "微信服务暂不可用"),
    INTERNAL_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误");

    private final int code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(int code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public int code() { return code; }
    public HttpStatus status() { return status; }
    public String message() { return message; }
}
