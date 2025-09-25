package com.financialfinishieldguard.data.manageUser.addUser;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;


@Data
public class AddUserDTO {
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
     * 用户名
     */
    @NotEmpty(message = "用户名不能为空")
    private String username;

}
