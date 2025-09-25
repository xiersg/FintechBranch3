package com.financialfinishieldguard.data.manageUser.addUser;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)//链式编程
public class AddUserVO {
    private String email;
}
