package com.financialfinishieldguard.data.user.updateAvatar;


import javax.validation.constraints.NotEmpty;

public class UpdateAvatarDTO {
    // AvatarUrl 头像地址
    @NotEmpty(message = "头像地址不能为空")
    public String avatarUrl;
}
