package com;

public enum ConfigEnum {
    SMS_ACCESS_KEY_ID("smsAccessKeyId","2031610355@qq.com"),
    SMS_ACCESS_KEY_SECRET("smsAccessKeySecret","omrxpibglkiicfij"),
    TOKEN_SECRET_KEY("tokenSecretKey","wushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuangwushuang");

    private final String value;

    private final String text;

    ConfigEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
}
