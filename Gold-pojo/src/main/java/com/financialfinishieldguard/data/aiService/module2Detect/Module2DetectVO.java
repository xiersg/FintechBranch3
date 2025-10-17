package com.financialfinishieldguard.data.aiService.module2Detect;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Module2DetectVO {

    private Boolean success;

    private Boolean is_fraudulent;

    private Double risk_score;

    private String content_type;
}
