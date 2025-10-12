package com.financialfinshieldguard.aiservice.ws;

import com.financialfinishieldguard.gateutils.constants.AuthConstant;
import com.financialfinishieldguard.gateutils.constants.MessageConstant;
import com.financialfinishieldguard.gateutils.constants.UserContext;
import com.financialfinishieldguard.gateutils.utils.JwtUtil;
import com.financialfinishieldguard.gateutils.utils.MessageUtil;
import com.financialfinshieldguard.aiservice.config.GetTokenConfig;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/chat", configurator = GetTokenConfig.class)
@Component
public class ChatEndpoint {

    private static final Map<Long, Session> userIdSessionMap = new ConcurrentHashMap<>();

    private static final MessageUtil messageUtil = new MessageUtil();

    @Autowired
    private JwtUtil jwtUtil;

    private Long userId;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        //解析token，获取userId并保存
        String token = (String) config.getUserProperties().get(AuthConstant.TOKEN);
        Long userId = getCurrentUserId(token);
        //存储userId
        this.userId = userId;
        //存userId与对应Session的映射
        userIdSessionMap.put(userId, session);

        //构建成功信息
        String message = messageUtil.getMessage(true, MessageConstant.WS_OPEN);
        //响应成功信息
        sentToCurrentUser(message);
    }

    @OnMessage
    public void onMessage(String message) {
        //构建信息
        String msg = messageUtil.getMessage(true, message);
        //响应信息
        sentToCurrentUser(msg);
    }

    @OnClose
    public void onClose(Session session) {
        //将userId和Session的映射关系移除\
        userIdSessionMap.remove(userId);
    }

    private void sentToCurrentUser(String message) {
        try {
            Session session = userIdSessionMap.get(userId);
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Long getCurrentUserId(String token) {
        Claims claims = jwtUtil.parse(token);
        String userIdStr = (String)claims.get(AuthConstant.USER_ID);
        return Long.valueOf(userIdStr);
    }

    /**
     * 推送消息给特定用户
     * @param userId
     * @param message
     */
    public void sendMessageToUser(Long userId, String message) {
        Session session = userIdSessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(messageUtil.getMessage(true, message));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
