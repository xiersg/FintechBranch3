package com.financialfinshieldguard.aiservice.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.financialfinishieldguard.data.websocket.UserToAIMessage;
import com.financialfinishieldguard.gateutils.constants.AuthConstant;
import com.financialfinishieldguard.gateutils.constants.MessageConstant;
import com.financialfinishieldguard.gateutils.utils.JwtUtil;
import com.financialfinishieldguard.gateutils.utils.RandomNumUtil;
import com.financialfinshieldguard.aiservice.config.GetTokenConfig;
import com.financialfinshieldguard.aiservice.service.impl.MessageManager;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/humanCustomer", configurator = GetTokenConfig.class)
@Component
@Slf4j
public class HumanCustomerEndpoint {

    private static ApplicationContext applicationContext;

    private MessageManager messageManager;

    private JwtUtil jwtUtil;

    private Long userId;

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // 手动获取 Bean
        this.jwtUtil = applicationContext.getBean(JwtUtil.class);
        this.messageManager = applicationContext.getBean(MessageManager.class);

        //解析token，获取userId并保存
        String token = (String) config.getUserProperties().get(AuthConstant.TOKEN);
        Long userId = getCurrentUserId(token);
        log.info("用户userId={}连接 用户-客服 Websocket...", userId);

        //存储userId
        this.userId = userId;
        //存userId与对应Session的映射
        messageManager.registerHumanCustomer(userId, session);

        //响应成功信息
        messageManager.sendHumanMessageToUserByUserId(userId, 0L, MessageConstant.WS_OPEN);
    }

    /**
     * 收到用户发的文本消息，调用AI接口获取数据，并将返回的信息流式传给当前用户
     *
     * @param message
     */
    @OnMessage
    public void onMessage(String message) {
        try {
            // 处理文本消息
            log.info("收到文本消息: " + message);
            ObjectMapper objectMapper = new ObjectMapper();
            UserToAIMessage userIdMessage = objectMapper.readValue(message, UserToAIMessage.class);
            //通过ws传给客服
             messageManager.sendHumanMessageToUserByUserId(userIdMessage.getSenderUserId(), userId, userIdMessage.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        try {
            if (this.userId != null) {
                //将userId和Session的映射关系移除
                messageManager.unregisterHumanCustomer(userId);
            }
        } catch (Exception e) {
            // 记录错误日志
            log.error("Error unregistering session for user: " + this.userId, e);
        } finally {
            // 确保资源释放
            this.userId = null;
        }
    }
    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("发生错误:{}", throwable.getMessage());
        throwable.printStackTrace();
    }

    private Long getCurrentUserId(String token) {
        Claims claims = jwtUtil.parse(token);
        String userIdStr = (String) claims.get(AuthConstant.USER_ID);
        return Long.valueOf(userIdStr);
    }

}
