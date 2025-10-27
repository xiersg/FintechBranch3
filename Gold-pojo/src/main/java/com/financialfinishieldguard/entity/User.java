package com.financialfinishieldguard.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * id
     */
    @TableId
    private Long userId;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

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
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 职位 0 普通用户 1 管理员 2 BOSS
     */
    private Long role;

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