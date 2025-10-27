package com.financialfinishieldguard.data.sessionService.saveMessage;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 对话中传的消息
 */
@Data
@Accessors(chain = true)
public class SaveMessageDTO {

    private Long senderUserId;

    private Long sessionId;

    private String content;
}
