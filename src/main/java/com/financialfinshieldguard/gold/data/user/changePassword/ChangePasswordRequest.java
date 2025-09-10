package com.financialfinshieldguard.gold.data.user.changePassword;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
@Accessors(chain = true)
public class ChangePasswordRequest {

    /**
     * 邮箱
     */
    @NotEmpty(message = "邮箱不能为空")
    @Length(min = 3, max = 32, message = "邮箱应为3-32位")
    private String email;


    /**
     * 新密码
     */
    @NotEmpty(message = "新密码不能为空")
    @Length(min = 6, max = 16, message = "密码应为 6-16 位")
    private String newPassword;

    /**
     * 验证码
     */
    @NotEmpty(message = "验证码不能为空")
    @Length(min = 6, max = 6, message = "验证码应为 6 位")
    private String code;
}
