package com.financialfinishieldguard.data.aiService.analyseImageText;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AnalyseImageTextVO {

    private Boolean success;
    private String filename;
    private String image_size;
    private String[] results;
    private Integer total_texts;
    private Double score;
}
