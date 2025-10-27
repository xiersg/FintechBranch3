package com.financialfinishieldguard.data.sessionService.getSessionHistory;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class SessionHistory {

    /**
     * 0-AI 1-用户
     */
    private Integer messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
