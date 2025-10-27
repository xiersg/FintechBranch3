package com.financialfinishieldguard.data.websocket;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserToAIMessage {

    /**
     * 当前用户的userId
     */
    private Long senderUserId;

    private Long sessionId;

    private String message;
}
