package com.financialfinishieldguard.data.aiService.analyseAudio;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Meta {

    private Double duration;

    private Double speech_ratio;
}
