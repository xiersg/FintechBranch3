package com.financialfinishieldguard.data.user.register;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)//链式编程
public class RegisterVO {
    private String email;
}
