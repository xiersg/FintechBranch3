package com.financialfinishieldguard.data.sessionService;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class HumanCustomerInfo {

    private String userId;

    private String email;

}
