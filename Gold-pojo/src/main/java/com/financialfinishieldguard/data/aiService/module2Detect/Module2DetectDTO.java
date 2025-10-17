package com.financialfinishieldguard.data.aiService.module2Detect;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Module2DetectDTO {

    private String content;

    private String content_type;

    private Boolean is_url;
}
