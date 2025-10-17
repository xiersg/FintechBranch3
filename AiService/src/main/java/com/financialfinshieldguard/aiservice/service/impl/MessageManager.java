package com.financialfinshieldguard.aiservice.service.impl;


import com.financialfinishieldguard.gateutils.utils.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 为了解决ChatEndpoint和ClientEndpointToAI为了传消息相互依赖的问题，创建了这个管理类！！！在实战中深刻理解了这一点
 */
@Component
public class MessageManager {

    private static final Map<Long, Session> userIdSessionMap = new ConcurrentHashMap<>();

    /**
     * websocket连接时注册添加
     *
     * @param userId
     * @param session
     */
    public void registerSession(Long userId, Session session) {
        userIdSessionMap.put(userId, session);
    }

    /**
     * websocket断开连接时除去
     *
     * @param userId
     */
    public void unregisterSession(Long userId) {
        userIdSessionMap.remove(userId);
    }

    /**
     * 传给服务器，userId默认为0，其余的正常传userId
     *
     * @param userId
     * @param message
     */
    public void sendMessageToUserByUserId(Long userId, String message) {
        Session session = userIdSessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                //在这里将信息构建为JSON格式
                session.getBasicRemote().sendText(MessageUtil.getMessage(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}