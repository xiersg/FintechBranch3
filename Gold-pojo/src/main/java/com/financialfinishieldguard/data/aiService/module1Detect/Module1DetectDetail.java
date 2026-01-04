package com.financialfinishieldguard.data.aiService.module1Detect;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Module1DetectDetail {
    private Double threshold;

    private String transaction_type;

    private Double amount;
}
