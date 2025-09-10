package com.financialfinshieldguard.gold.data.user.updateAvatar;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateAvatarResponse {
    /**
     * 用户ID
     */
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
     * 用户头像
     */
    private String avatar;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 性别 0 男 1 女 2 保密
     */
    private Integer gender;

    /**
     * 用户状态 1 正常 2 封装 3 注销
     */
    private Integer status;
}
