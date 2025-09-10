package com.financialfinshieldguard.gold.data.user.loginCode;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
@Accessors(chain = true)
public class LoginCodeRequest {

    /**
     * 邮箱
     */
    @NotEmpty(message = "邮箱不能为空")
    @Length(min = 3, max = 32, message = "邮箱应为3-32位")
    private String email;

    /**
     * 验证码
     */
    @NotEmpty(message = "验证码不能为空")
    @Length(min = 6, max = 6, message = "验证码应为 6 位")
    private String code;
}

