package com.financialfinishieldguard.data.websocket;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SingleMsg {

    private String role;

    private String content;
}
