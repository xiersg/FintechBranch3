package com.financialfinshieldguard.gold.data.user.login;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginResponse {
    private Long  userId;
    private String  userName;
    private String avatar;
    private String signature;
    private Integer gender;
    private Integer status;
    private String token;
}
