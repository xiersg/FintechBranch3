package com.financialfinishieldguard.data.complaint;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;

@Data
@Accessors(chain = true)
public class ComplaintDTO {

    /**
     * 投诉内容
     */
    @NotEmpty(message = "投诉内容不可为空")
    private String text;

}
