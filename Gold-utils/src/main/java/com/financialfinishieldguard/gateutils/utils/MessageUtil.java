package com.financialfinishieldguard.gateutils.utils;

import com.alibaba.fastjson.JSON;
import com.financialfinishieldguard.data.websocket.Message;

/**
 * 构建消息用于Websocket传递，将消息实体类转为JSON格式
 */
public class MessageUtil {

    public String getMessage(Boolean isSuccess, String msg) {
        Message message = new Message().setIsSuccess(isSuccess).setMessage(msg);
        return JSON.toJSONString(message);
    }

}
