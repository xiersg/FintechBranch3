package com.financialfinishieldguard.data.manageUser.getUsersInfo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class UserInfo {

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String userName;

//    /**
//     * 密码
//     */
//    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 性别 0 男 1 女 2 保密
     */
    private Integer gender;

    /**
     * 用户状态。1正常，2封禁，3注销
     */
    private Integer status;


    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 职责
     */
    private Long role;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;


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
