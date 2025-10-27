package com.financialfinishieldguard.data.websocket;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WebsocketToAIMessage {

    private Long user_id;

    private Long session_id;

    private String content;
}
