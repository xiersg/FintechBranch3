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

    private static final Map<Long, Session> humanCustomerMap = new ConcurrentHashMap<>();



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
     * websocket连接客服
     * @param userId
     * @param session
     */
    public void registerHumanCustomer(Long userId, Session session) {
        humanCustomerMap.put(userId, session);
    }

    /**
     * websocket断开连接客服
     * @param userId
     */
    public void unregisterHumanCustomer(Long userId) {
        humanCustomerMap.remove(userId);
    }

    /**
     * 传给另外一个用户（比如人工客服），userId默认为0，其余的正常传userId
     * @param userId 要传给的对象
     * @param fromUserId 发信息的用户
     * @param message 消息
     */
    public void sendAIChatMessageToUserByUserId(Long userId, Long fromUserId, Long sessionId, String message, String type) {
        Session session = userIdSessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                //在这里将信息构建为JSON格式
                session.getBasicRemote().sendText(MessageUtil.getMessage(fromUserId, sessionId, message, type));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 传给AI
     * @param userId 要传给的对象
     * @param fromUserId 发信息的用户
     * @param message 消息
     */
    public void sendAIChatMessageToAI(Long userId, Long fromUserId, Long sessionId, String message) {
        Session session = userIdSessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                //在这里将信息构建为JSON格式
                session.getBasicRemote().sendText(MessageUtil.getMessageToAI(fromUserId, sessionId, message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 用户-客服交流
     * @param userId
     * @param fromUserId
     * @param message
     */
    public void sendHumanMessageToUserByUserId(Long userId,Long fromUserId, String message) {
        Session session = humanCustomerMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                //在这里将信息构建为JSON格式
                session.getBasicRemote().sendText(MessageUtil.getHumanCustomerMessage(fromUserId, message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}