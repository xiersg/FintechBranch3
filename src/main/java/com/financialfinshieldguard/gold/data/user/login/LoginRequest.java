package com.financialfinshieldguard.gold.data.user.login;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
@Accessors(chain = true)
public class LoginRequest {

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
}
