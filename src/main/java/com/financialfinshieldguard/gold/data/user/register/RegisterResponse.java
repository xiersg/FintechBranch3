package com.financialfinshieldguard.gold.data.user.register;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)//链式编程
public class RegisterResponse {
    private String email;
}
