package com.financialfinshieldguard.gold.constants.user;

import lombok.Getter;

@Getter
public enum ErrorEnum {
    //400 01 02 03
    //500 01 02 03
    SUCCESS(200, "ok"),
    REGISTER_ERROR(40001, "注册失败，用户已存在"),
    CODE_ERROR(40002, "验证码错误"),
    LOGIN_ERROR(40003, "登录失败，用户名或密码错误"),
    NO_USER_ERROR(40004, "用户不存在"),
    NO_USER_LOGIN(40005, "用户未登录"),
    JWT_ERROR(40006, "jwt令牌无法正确解析"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    UPDATE_AVATAR_ERROR(50001, "用户更新头像失败"),

    DATABASE_ERROR(50013, "数据库异常");
    private final int code;

    private final String message;

    ErrorEnum(int code, String message) {
        this.code = code;

        this.message = message;
    }
}
