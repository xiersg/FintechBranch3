package com.financialfinishieldguard.data.websocket;

import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.ref.PhantomReference;

@Data
@Accessors(chain = true)
public class AIMsgDTO {

    private String type;

    private Long session_id;

    private Long user_id;

    private String delta;
}
