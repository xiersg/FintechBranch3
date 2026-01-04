package com.financialfinishieldguard.data.websocket;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WebsocketMessage {

    private String type;

    private String fromUserId;

    private String sessionId;

    private String message;


}
