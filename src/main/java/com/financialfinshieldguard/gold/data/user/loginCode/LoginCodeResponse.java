package com.financialfinshieldguard.gold.data.user.loginCode;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginCodeResponse {
    private String  userId;
    private String  userName;
//    private String avatar;
    private String signature;
    private Integer gender;
    private Integer status;
    private String token;
}
