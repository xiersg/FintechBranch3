package com.financialfinshieldguard.gold.data.user.register;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
public class RegisterRequest {
    /**
     * 邮箱
     */
    @NotEmpty(message = "邮箱不能为空")
    @Length(min = 3, max = 32, message = "邮箱应为3-32位")
    private String email;

    /**
     * 密码
     */
    @NotEmpty(message = "密码不能为空")
    @Length(min = 6, max = 16, message = "密码应为 6-16 位")
    private String password;

    /**
     * 验证码
     */
    @NotEmpty(message = "验证码不能为空")
    @Length(min = 6, max = 6, message = "验证码应为 6 位")
    private String code;

}
