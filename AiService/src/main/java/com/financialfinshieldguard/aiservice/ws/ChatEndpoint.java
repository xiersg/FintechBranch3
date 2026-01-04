package com.financialfinshieldguard.aiservice.ws;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financialfinishieldguard.data.sessionService.saveMessage.SaveMessageDTO;
import com.financialfinishieldguard.data.websocket.SingleMsg;
import com.financialfinishieldguard.entity.SessionMessages;
import com.financialfinishieldguard.gateutils.constants.AuthConstant;
import com.financialfinishieldguard.gateutils.constants.MessageConstant;
import com.financialfinishieldguard.gateutils.exception.UserException;
import com.financialfinishieldguard.gateutils.utils.JwtUtil;
import com.financialfinishieldguard.gateutils.utils.RandomNumUtil;
import com.financialfinshieldguard.aiservice.config.GetTokenConfig;
import com.financialfinshieldguard.aiservice.service.AiService;
import com.financialfinshieldguard.aiservice.service.SessionMessagesService;
import com.financialfinshieldguard.aiservice.service.impl.MessageManager;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@ServerEndpoint(value = "/chat", configurator = GetTokenConfig.class)
@Component
@Slf4j
public class ChatEndpoint {

    private static ApplicationContext applicationContext;

    private MessageManager messageManager;

    private JwtUtil jwtUtil;

    private SessionMessagesService sessionMessagesService;

    private Long userId;

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // 手动获取 Bean
        this.jwtUtil = applicationContext.getBean(JwtUtil.class);
        this.messageManager = applicationContext.getBean(MessageManager.class);
        this.sessionMessagesService = applicationContext.getBean(SessionMessagesService.class);

        //解析token，获取userId并保存
        String token = (String) config.getUserProperties().get(AuthConstant.TOKEN);
        Long userId = getCurrentUserId(token);
        //存储userId
        this.userId = userId;
        //存userId与对应Session的映射
        messageManager.registerSession(userId, session);

        //响应成功信息
        messageManager.sendAIChatMessageToUserByUserId(userId, 0L, null, MessageConstant.WS_OPEN, "system");
    }

    /**
     * 收到用户发的文本消息，调用AI接口获取数据，并将返回的信息流式传给当前用户
     *
     * @param message
     */
    @OnMessage
    public void onMessage(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        SaveMessageDTO saveMessageDTO = null;
        try {
            saveMessageDTO = objectMapper.readValue(message, SaveMessageDTO.class);
        } catch (JsonProcessingException e) {
            throw new UserException("前端发送的websocket内容转为SaveMessageDTO失败！！！请检查结构数据正确性！！！");
        }

        // 处理文本消息
        log.info("收到文本消息: " + message);
        //保存消息
        sessionMessagesService.saveMessage(1, saveMessageDTO);

        //查询当前对话的所有历史信息
        List<SessionMessages> historyMessage = sessionMessagesService.getHistoryBySessionIdToAI(saveMessageDTO.getSessionId());

        List<String> result = historyMessage.stream().map(single -> {
            SingleMsg msg  = new SingleMsg();
            if (single.getMessageType() == 1) {
                msg = new SingleMsg().setContent(single.getContent()).setRole("user");
            } else {
                msg = new SingleMsg().setContent(single.getContent()).setRole("assistant");
            }
            return JSON.toJSONString(msg);
        }).collect(Collectors.toList());


        //通过ws传给AI
        messageManager.sendAIChatMessageToAI(0L, saveMessageDTO.getSenderUserId(), saveMessageDTO.getSessionId(), result.toString());

    }

    /**
     * 收到音频信息，调用AI分析
     *
     * @param bytes
     */
    @OnMessage
    public void onMessage(ByteBuffer bytes) {
        // 处理二进制消息
        log.info("收到音频信息:" + bytes);
        // 处理图片消息
        // 将ByteBuffer转换为字节数组
        byte[] audioBytes = new byte[bytes.remaining()];
        bytes.get(audioBytes);

        // 创建MockMultipartFile对象
        MultipartFile audioMultipartFile = new MockMultipartFile(
                "file", // 参数名
                RandomNumUtil.getRandomNum() + "audioName.wav", // 原始文件名
                "audio/wav", // 文件的MIME类型
                audioBytes // 文件内容
        );
        analyseAudio(audioMultipartFile);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.info("与用户{}的websocket连接关闭,原因:{}", userId, closeReason.getReasonPhrase());
        //将userId和Session的映射关系移除
        messageManager.unregisterSession(userId);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("发生错误:{}", throwable.getMessage());
        throwable.printStackTrace();
    }
    /**
     * 加了一个MessageManager，故这段代码不需要了
     * @param token
     * @return
     */
//    private void sentToCurrentUser(String message) {
//        try {
//            Session session = userIdSessionMap.get(userId);
//            session.getBasicRemote().sendText(message);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    /**
     * //     * 推送消息给特定用户
     * //     *
     * //     * @param message
     * //
     */
//    public void sendMessageToUser(String message) {
//        Session session = userIdSessionMap.get(userId);
//        if (session != null && session.isOpen()) {
//            try {
//                session.getBasicRemote().sendText(messageUtil.getMessage(message));
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
    private Long getCurrentUserId(String token) {
        Claims claims = jwtUtil.parse(token);
        String userIdStr = (String) claims.get(AuthConstant.USER_ID);
        return Long.valueOf(userIdStr);
    }


    /**
     * 通过websocket传音频文件，判断AI率
     *
     * @param file
     * @return
     */
    public void analyseAudio(MultipartFile file) {
        try {
            // 创建 HttpClient 对象
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // 创建请求对象
            HttpPost httpPost = new HttpPost("http://13425.free.idcfengye.com/anti_spoof_score");

            // 检查传入的文件是否为空
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("文件不能为空");
            }

            // 使用 MultipartEntityBuilder 构建请求体
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody(
                    "file", // 参数名
                    file.getInputStream(), // 音频文件对象
                    ContentType.create("audio/wav"), // 音频文件的 MIME 类型
                    file.getOriginalFilename() // 音频文件名
            );

            // 构建 HttpEntity
            HttpEntity multipartEntity = builder.build();

            // 设置请求体
            httpPost.setEntity(multipartEntity);
            //发送请求
            CloseableHttpResponse response = httpClient.execute(httpPost);
            //解析返回结果
            //获取服务端返回回来的状态码
            int statusCode = response.getStatusLine().getStatusCode();

            System.out.println("服务端返回的状态码: " + statusCode);

            //获取服务端返回回来的响应体，然后通过一个工具类来解析这个响应体
            HttpEntity entity1 = response.getEntity();
            String body = EntityUtils.toString(entity1);

            System.out.println("服务端返回的数据是: " + body);

            //现在改成ws，本来就要传JSON结构，就不需要再转实体类了！直接把body发给前端
            messageManager.sendAIChatMessageToUserByUserId(userId, 0L, null, body, "ai");

            //关闭资源
            response.close();
            httpClient.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
