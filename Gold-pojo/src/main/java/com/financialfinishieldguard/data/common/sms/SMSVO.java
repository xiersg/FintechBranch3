package com.financialfinishieldguard.data.common.sms;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SMSVO {
    private String phone;
}
