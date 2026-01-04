package com.financialfinishieldguard.data.aiService.module1Detect;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Module1DetectDTO {

    private Integer step;

    private String type;

    private Integer amount;

    private String nameOrig;

    private String nameDest;

    private Integer oldbalanceOrg;

    private Integer newbalanceOrig;

    private Integer oldbalanceDest;

    private Integer newbalanceDest;

}
