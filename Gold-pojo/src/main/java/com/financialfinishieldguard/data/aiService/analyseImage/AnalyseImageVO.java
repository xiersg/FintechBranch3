package com.financialfinishieldguard.data.aiService.analyseImage;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AnalyseImageVO {

    private Boolean success;

    private Boolean is_fraudulent;

    private Double risk_score;

    private String content_type;

    private String content_preview;
}
