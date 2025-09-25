package com.financialfinishieldguard.data.complaint;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ComplaintVO {

    private boolean isComplainted;
}
