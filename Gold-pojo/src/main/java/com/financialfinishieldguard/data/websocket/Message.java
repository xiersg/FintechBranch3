package com.financialfinishieldguard.data.websocket;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Message {

    private Boolean isSuccess;

    private String message;

}
