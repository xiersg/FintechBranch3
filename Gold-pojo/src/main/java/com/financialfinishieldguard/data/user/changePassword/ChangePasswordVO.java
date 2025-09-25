package com.financialfinishieldguard.data.user.changePassword;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChangePasswordVO {
    private String email;
}
