package com.financialfinshieldguard.gold.constants.config;

import lombok.Getter;

@Getter
public enum TimeOutEnum {
    JWT_TIME_OUT("token time out(hour)", "jwt:", 24);
    private final String name;
    private final String prefix;
    private final int timeOut;

    TimeOutEnum(String name, String prefix, int timeOut) {
        this.name = name;
        this.prefix = prefix;
        this.timeOut = timeOut;
    }
}
