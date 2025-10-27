package com.financialfinshieldguard.aiservice.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialfinishieldguard.data.sessionService.saveMessage.SaveMessageDTO;
import com.financialfinishieldguard.data.websocket.UserToAIMessage;
import com.financialfinishieldguard.gateutils.exception.UserException;
import com.financialfinshieldguard.aiservice.service.SessionMessagesService;
import com.financialfinshieldguard.aiservice.service.impl.MessageManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@ClientEndpoint
@Component
public class ClientEndpointToAI extends Endpoint{

    private static ApplicationContext applicationContext;

    private MessageManager messageManager;

//    private JwtUtil jwtUtil;

    private SessionMessagesService sessionMessagesService;

//    private Long userId;

    private WebSocketContainer container;

    @Value("${ai.url.ai-webSocket-url}")
    private String aiWebSocketUrl;


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
//        this.jwtUtil = applicationContext.getBean(JwtUtil.class);
        this.messageManager = applicationContext.getBean(MessageManager.class);
        this.sessionMessagesService = applicationContext.getBean(SessionMessagesService.class);

        // 设置消息缓冲区大小
        session.setMaxBinaryMessageBufferSize(1024);
        session.setMaxTextMessageBufferSize(1024);
//        //解析token，获取userId并保存
//        String token = (String) config.getUserProperties().get(AuthConstant.TOKEN);
//        Long userId = getCurrentUserId(token);
//        //存储userId
//        this.userId = userId;
        // 保存Session对象!!!
        messageManager.registerSession(0L, session);
        log.info("连接成功");

        startHeartbeat(session);
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("收到原始信息:{}", message);
        ObjectMapper objectMapper = new ObjectMapper();
        SaveMessageDTO saveMessageDTO;
        try {
            saveMessageDTO = objectMapper.readValue(message, SaveMessageDTO.class);
        } catch (JsonProcessingException e) {
            throw new UserException("发送的websocket内容转为SaveMessageDTO失败！！！请检查结构数据正确性！！！");
        }

        log.info("收到AI返回的信息:{}", message);
        //保存消息
        sessionMessagesService.saveMessage(0, saveMessageDTO);

        UserToAIMessage msg;
        try {
            //解析消息
            msg = objectMapper.readValue(message, UserToAIMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //构建消息，发给前端
        messageManager.sendAIChatMessageToUserByUserId(msg.getSenderUserId(), 0L, msg.getSessionId() ,msg.getMessage());
        log.info("信息已成功传给前端~");


    }

    @OnMessage
    public void onPong(PongMessage pongMessage) {
        log.info("收到服务端 Ping，自动回复了 Pong");
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

            // 创建与浏览器完全相同的配置
            ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                    .configurator(new ClientEndpointConfig.Configurator() {
                        @Override
                        public void beforeRequest(Map<String, List<String>> headers) {
                            // 完全复制浏览器的请求头
                            headers.put("Accept-Encoding", Arrays.asList("gzip, deflate"));
                            headers.put("Accept-Language", Arrays.asList("zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6"));
                            headers.put("Cache-Control", Arrays.asList("no-cache"));
                            headers.put("Connection", Arrays.asList("Upgrade"));
                            headers.put("Host", Arrays.asList("13425.free.idcfengye.com"));
                            headers.put("Origin", Arrays.asList("null"));  // 重要：浏览器发送的是 null
                            headers.put("Pragma", Arrays.asList("no-cache"));
                            headers.put("Sec-WebSocket-Extensions", Arrays.asList("permessage-deflate; client_max_window_bits"));
                            headers.put("Sec-WebSocket-Key", Arrays.asList(generateWebSocketKey()));
                            headers.put("Sec-WebSocket-Version", Arrays.asList("13"));
                            headers.put("Upgrade", Arrays.asList("websocket"));
                            headers.put("User-Agent", Arrays.asList(
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 Edg/141.0.0.0"
                            ));

                            log.info("🔍 设置的请求头: {}", headers);
                        }
                    })
                    .build();

            log.info("🔍 正在建立 WebSocket 连接...");

            // 建立连接
            Session session = container.connectToServer(this, config, serverUri);

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

    /**
     * 生成 WebSocket 握手 Key
     * 这是 Base64 编码的 16 字节随机数
     */
    private String generateWebSocketKey() {
        byte[] randomBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(randomBytes);
        return java.util.Base64.getEncoder().encodeToString(randomBytes);
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
//    /**
//     * 连接到 AI WebSocket 服务
//     */
//    public boolean connectToAIEndpoint() {
//        try {
//            URI serverUri = new URI(aiWebSocketUrl);
//
//            // 创建客户端配置，模拟浏览器行为
//            ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
//                    .configurator(new ClientEndpointConfig.Configurator() {
//                        @Override
//                        public void beforeRequest(Map<String, List<String>> headers) {
//                            // 添加浏览器常见的请求头
//                            headers.put("User-Agent", Arrays.asList(
//                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 Edg/141.0.0.0"
//                            ));
//                            headers.put("Sec-WebSocket-Protocol", Arrays.asList("chat"));
//                            log.info("🔍 请求头: {}", headers);
//                        }
//                    })
//                    .build();
//
//            log.info("🔍 正在建立 WebSocket 连接...");
//
//            // 建立连接
//            //TODO: 这里一直有个bug就是说一直冒红，明明有这个方法但是冒红解析不出来，结果显性继承  extends Endpoint 之后bug就解决了，但是为什么@ClientEndpoint写了这个注解不够呢？？？
//            container.connectToServer(this, config, serverUri);
//
//
////            /**
////             * •	connectToServer：建立 WebSocket 连接的核心方法
////             * •	this：当前对象作为 WebSocket 端点（必须是 @ClientEndpoint 注解的类）
////             * •	new URI("...")：目标 WebSocket 服务器的地址
////             */
////            // 建立连接
////            container.connectToServer(this, serverUri);
//
//            log.info("成功连接到 AI WebSocket 服务: {}", serverUri);
//            return true;
//
//        } catch (Exception e) {
//            log.error("连接 AI WebSocket 服务失败", e);
//            return false;
//        }
//    }

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

//    private Long getCurrentUserId(String token) {
////        Claims claims = jwtUtil.parse(token);
////        String userIdStr = (String) claims.get(AuthConstant.USER_ID);
//        return Long.valueOf(userIdStr);
//    }
}
