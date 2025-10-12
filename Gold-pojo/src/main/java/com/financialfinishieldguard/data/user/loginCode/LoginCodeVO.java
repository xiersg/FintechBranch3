package com.financialfinishieldguard.data.user.loginCode;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginCodeVO {
    private Long  userId;
    private String  userName;
//    private String avatar;
    private String signature;
    private Integer gender;
    private Integer status;
    private Long role;
    private String token;
    /**
     * 习惯
     */
    private String habit;

    /**
     * 投资偏好
     */
    private String investmentPreference;

    /**
     * 教育背景
     */
    private String educationalBackground;

    /**
     * 投资预算
     */
    private String investmentBudget;

    /**
     * 事业
     */
    private String career;
}
