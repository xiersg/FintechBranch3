package com.financialfinshieldguard.gold.data.common.sms;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SMSRequest {
    private String phone;
    private String password;
    private String code;
}
