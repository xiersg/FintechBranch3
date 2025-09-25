package com.financialfinishieldguard.gateutils.constants.enumm;

import lombok.Getter;

/**
 * 职位枚举
 */
@Getter
public enum RoleEnum {
    /**
     * 用户
     */
    USER("普通用户", 0L),

    /**
     * 管理员
     */
    ADMIN("管理员", 1L),

    /**
     * BOSS
     */
    BOSS("BOSS", 2L);

    private final String key;

    private final Long value;

    RoleEnum(String key, Long value) {
        this.key = key;
        this.value = value;
    }
}
