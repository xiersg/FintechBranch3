package com.financialfinishieldguard.data.aiService.module1Detect;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Module1DetectVO {

    private String transaction_id;

    private Boolean is_fraud;

    private Double fraud_score;

    private Module1DetectDetail details;

}
