package com.financialfinshieldguard.aiservice.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialfinishieldguard.data.sessionService.saveMessage.SaveMessageDTO;
import com.financialfinishieldguard.data.websocket.AIMsgDTO;
import com.financialfinishieldguard.data.websocket.PongMsg;
import com.financialfinishieldguard.data.websocket.UserToAIMessage;
import com.financialfinishieldguard.gateutils.constants.AIConstant;
import com.financialfinishieldguard.gateutils.exception.UserException;
import com.financialfinshieldguard.aiservice.service.SessionMessagesService;
import com.financialfinshieldguard.aiservice.service.impl.MessageManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.PingMessage;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@ClientEndpoint
@Component
public class ClientEndpointToAI {

    private static ApplicationContext applicationContext;

    private MessageManager messageManager;

//    private JwtUtil jwtUtil;

    private SessionMessagesService sessionMessagesService;

//    private Long userId;

    private WebSocketContainer container;

    @Value("${ai.url.ai-webSocket-url}")
    private String aiWebSocketUrl;

    // 使用 ConcurrentHashMap 来为每个用户维护一个独立的 StringBuilder
    private final ConcurrentHashMap<Long, StringBuilder> userMessageBuffer = new ConcurrentHashMap<>();


    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }


    @PostConstruct
    public void init() {
        //•	ContainerProvider：WebSocket API 的服务提供者类
        //•	getWebSocketContainer()：获取 WebSocket 容器的静态方法
        //•	作用：获得一个管理 WebSocket 连接的基础设施实例
        this.container = ContainerProvider.getWebSocketContainer();
        this.container.setDefaultMaxSessionIdleTimeout(60000); // 设置超时时间为60秒
        // 如果AI服务不可用，会影响应用启动,因此采用延迟连接
        scheduleInitialConnection();
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // 手动获取 Bean
        this.messageManager = applicationContext.getBean(MessageManager.class);
        this.sessionMessagesService = applicationContext.getBean(SessionMessagesService.class);

        // 设置消息缓冲区大小
        session.setMaxBinaryMessageBufferSize(1024);
        session.setMaxTextMessageBufferSize(1024);

        // 保存Session对象!!!
        messageManager.registerSession(0L, session);
        log.info("连接成功");

        startHeartbeat(session);
    }


    @OnMessage
    public void onMessage(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            //readTree 方法将 JSON 字符串解析为一个 JsonNode 对象，这个对象可以方便地访问 JSON 数据中的各个字段。
            JsonNode rootNode = objectMapper.readTree(message);
            //asText()：将获取到的 JsonNode 对象转换为字符串。
            String type = rootNode.get(AIConstant.TYPE).asText();
            if ("ping".equals(type)) {
                // 处理心跳回应
//                log.info("收到ping消息");


            } else if ("delta".equals(type)) {
                log.info("收到delta消息:{}", message);
                // 处理AI返回的增量消息
                AIMsgDTO msg = objectMapper.treeToValue(rootNode, AIMsgDTO.class);
                // 获取或创建该用户的 StringBuilder
                userMessageBuffer.computeIfAbsent(msg.getUser_id(), k -> new StringBuilder()).append(msg.getDelta());

                messageManager.sendAIChatMessageToUserByUserId(msg.getUser_id(), 0L, msg.getSession_id(), msg.getDelta(), msg.getType());


            } else if ("result".equals(type)) {
                log.info("收到result消息:{}", message);
                AIMsgDTO msg = objectMapper.treeToValue(rootNode, AIMsgDTO.class);
                StringBuilder completeMessageBuilder = userMessageBuffer.get(msg.getUser_id());
                if (completeMessageBuilder != null) {
                    String completeMessage = completeMessageBuilder.toString();
                    // 保存到数据库
                    sessionMessagesService.saveMessage(0, new SaveMessageDTO().setSenderUserId(msg.getUser_id()).setSessionId(msg.getSession_id()).setContent(completeMessage));
                    // 从 ConcurrentHashMap 中移除该用户的 StringBuilder
                    userMessageBuffer.remove(msg.getUser_id());

                    messageManager.sendAIChatMessageToUserByUserId(msg.getUser_id(), 0L, msg.getSession_id(), AIConstant.NULL , msg.getType());
                } else {
                    log.warn("未找到用户 {} 的消息缓冲区", msg.getUser_id());
                }

            } else {
                log.warn("未知消息类型: {}", type);
            }
        } catch (JsonProcessingException e) {
            log.error("消息解析失败: {}", e.getMessage());
            // 不要抛出异常，否则可能会关闭连接
        }
    }


    @OnMessage
    public void onPong(PongMessage pongMessage) {
//        log.info("收到服务端 Ping，自动回复了 Pong");
        // 在 Java WebSocket API 中，收到 Ping 会自动回复 Pong
        // 这个方法只是用于确认收到了 Ping
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
            log.info("尝试重新连接AI服务...");
            boolean connected = connectToAIEndpoint();
            if (!connected) {
                // 连接失败，继续重试
                scheduleReconnect();
            }
            scheduler.shutdown();
        }, 5, TimeUnit.SECONDS);
    }

    public boolean connectToAIEndpoint() {
        try {
            log.info("🔍 开始连接 AI WebSocket 服务...");
            log.info("🔍 目标URL: {}", aiWebSocketUrl);

            URI serverUri = new URI(aiWebSocketUrl);

            log.info("🔍 正在建立 WebSocket 连接...");

            // 建立连接
            Session session = container.connectToServer(this, serverUri);

            return true;

        } catch (Exception e) {
            log.error("❌ 连接失败: {}", e.getMessage());
            log.error("❌ 异常类型: {}", e.getClass().getName());

            if (e instanceof javax.websocket.DeploymentException) {
                log.error("❌ 部署异常 - 检查URL和服务器配置");
            } else if (e instanceof java.net.ConnectException) {
                log.error("❌ 连接被拒绝 - 检查服务器是否运行");
            } else if (e instanceof java.net.UnknownHostException) {
                log.error("❌ 未知主机 - 检查域名解析");
            }

            e.printStackTrace();
            return false;
        }
    }



    private void startHeartbeat(Session session) {
        ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            if (session != null && session.isOpen()) {
                try {
                    ByteBuffer heartbeatData = ByteBuffer.wrap("HEARTBEAT".getBytes());
                    session.getBasicRemote().sendPing(heartbeatData);
                    log.debug("发送心跳 Ping");
                } catch (IOException e) {
                    log.error("发送心跳失败: {}", e.getMessage());
                    // 考虑重连逻辑
                    scheduleReconnect();
                }
            }
        }, 0, 30, TimeUnit.SECONDS); // 每30秒发送一次
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
