package com.financialfinishieldguard.gateutils.utils;

import com.alibaba.fastjson.JSON;
import com.financialfinishieldguard.data.websocket.WebsocketMessage;
import com.financialfinishieldguard.data.websocket.WebsocketToAIMessage;
import org.springframework.stereotype.Component;

/**
 * 构建消息用于Websocket传递，将消息实体类转为JSON格式
 */
public class MessageUtil {

    public static String getMessage(Long fromUserId, Long sessionId, String msg, String type) {
        WebsocketMessage message = new WebsocketMessage().setType(type).setFromUserId(fromUserId.toString()).setSessionId(sessionId.toString()).setMessage(msg);
        return JSON.toJSONString(message);
    }


    public static String getMessageToAI(Long fromUserId, Long sessionId, String msg) {
        WebsocketToAIMessage message = new WebsocketToAIMessage().setUser_id(fromUserId).setSession_id(sessionId).setContent(msg);
        return JSON.toJSONString(message);
    }

    public static String getHumanCustomerMessage(Long fromUserId, String msg) {
        WebsocketMessage message = new WebsocketMessage().setFromUserId(fromUserId.toString()).setMessage(msg);
        return JSON.toJSONString(message);
    }

}
