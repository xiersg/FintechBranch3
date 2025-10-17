package com.financialfinishieldguard.gateutils.utils;

import com.alibaba.fastjson.JSON;
import com.financialfinishieldguard.data.websocket.WebsocketMessage;
import org.springframework.stereotype.Component;

/**
 * 构建消息用于Websocket传递，将消息实体类转为JSON格式
 */
public class MessageUtil {

    public static String getMessage(String msg) {
        WebsocketMessage message = new WebsocketMessage().setMessage(msg);
        return JSON.toJSONString(message);
    }

}
