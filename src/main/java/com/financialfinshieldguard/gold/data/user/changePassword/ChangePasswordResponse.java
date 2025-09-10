package com.financialfinshieldguard.gold.data.user.changePassword;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChangePasswordResponse {
    private String email;
}
