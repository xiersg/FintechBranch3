package com.financialfinishieldguard.data.complaint;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ComplaintInfo {

    private Long id;

    private String text;

    private LocalDateTime createdAt;

}
