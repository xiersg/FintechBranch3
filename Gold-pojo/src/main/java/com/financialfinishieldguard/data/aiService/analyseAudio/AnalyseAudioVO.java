package com.financialfinishieldguard.data.aiService.analyseAudio;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AnalyseAudioVO {

    private Double spoof_prob;

    private String label;

    private Boolean valid;

    private String reason;

    private Meta meta;

}
