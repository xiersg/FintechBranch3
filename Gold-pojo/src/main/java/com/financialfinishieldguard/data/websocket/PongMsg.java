package com.financialfinishieldguard.data.websocket;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PongMsg {

    private String type;
}
