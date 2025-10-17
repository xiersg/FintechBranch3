package com.financialfinshieldguard.aiservice.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialfinishieldguard.data.websocket.UserIdMessage;
import com.financialfinshieldguard.aiservice.service.impl.MessageManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@ClientEndpoint
@Component
public class ClientEndpointToAI {

    @Autowired
    private MessageManager messageManager;

    private WebSocketContainer container;

    @Value("${ai.url.ai-webSocket-url}")
    private String aiWebSocketUrl;

    @PostConstruct
    public void init() {
        //•	ContainerProvider：WebSocket API 的服务提供者类
        //•	getWebSocketContainer()：获取 WebSocket 容器的静态方法
        //•	作用：获得一个管理 WebSocket 连接的基础设施实例
        this.container = ContainerProvider.getWebSocketContainer();
        // 如果AI服务不可用，会影响应用启动,因此采用延迟连接
        scheduleInitialConnection();
    }

    @OnOpen
    public void onOpen(Session session) {
        // 保存Session对象!!!
        messageManager.registerSession(0L, session);
        log.info("连接成功");
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("收到AI返回的信息:{}", message);
        UserIdMessage msg = new UserIdMessage();
        try {
            //解析消息
            ObjectMapper objectMapper = new ObjectMapper();
            msg = objectMapper.readValue(message, UserIdMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //构建消息，发给前端
        messageManager.sendMessageToUserByUserId(msg.getUserId(), msg.getMessage());
        log.info("信息已成功传给前端~");
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.info("与AI的websocket连接关闭,原因:{}", closeReason);
        //将userId和Session的映射关系移除
        messageManager.unregisterSession(0L);

        // 自动重连（排除正常关闭的情况）
        if (!closeReason.getCloseCode().equals(CloseReason.CloseCodes.NORMAL_CLOSURE)) {
            scheduleReconnect();
        }
    }


    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("发生错误:{}", throwable.getMessage());
        // 根据错误类型决定是否重连
        if (throwable instanceof IOException) {
            scheduleReconnect();
        }
    }

    private void scheduleInitialConnection() {
        // 延迟5秒连接，给应用启动留出时间
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            connectToAIEndpoint();
            scheduler.shutdown();
        }, 5, TimeUnit.SECONDS);
    }


    /**
     * 连接到 AI WebSocket 服务
     */
    public boolean connectToAIEndpoint() {
        try {
            URI serverUri = new URI(aiWebSocketUrl);

            /**
             * •	connectToServer：建立 WebSocket 连接的核心方法
             * •	this：当前对象作为 WebSocket 端点（必须是 @ClientEndpoint 注解的类）
             * •	new URI("...")：目标 WebSocket 服务器的地址
             */
            // 建立连接
            container.connectToServer(this, serverUri);

            log.info("成功连接到 AI WebSocket 服务: {}", serverUri);
            return true;

        } catch (Exception e) {
            log.error("连接 AI WebSocket 服务失败", e);
            return false;
        }
    }

    private void scheduleReconnect() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            log.info("尝试重新连接AI服务...");
            boolean connected = connectToAIEndpoint();
            if (!connected) {
                // 连接失败，继续重试
                scheduleReconnect();
            }
            scheduler.shutdown();
        }, 10, TimeUnit.SECONDS); // 10秒后重试
    }

}
