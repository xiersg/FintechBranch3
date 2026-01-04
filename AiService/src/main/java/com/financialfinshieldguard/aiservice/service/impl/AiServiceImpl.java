package com.financialfinshieldguard.aiservice.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialfinishieldguard.data.aiService.analyseAudio.AnalyseAudioVO;
import com.financialfinishieldguard.data.aiService.analyseImage.AnalyseImageVO;
import com.financialfinishieldguard.data.aiService.analyseImageText.AnalyseImageTextVO;
import com.financialfinishieldguard.data.aiService.getCurrentUserDialogues.GetCurrentUserDialoguesVO;
import com.financialfinishieldguard.data.aiService.getCurrentUserDialogues.UserDialogueInfo;
import com.financialfinishieldguard.data.aiService.module1Detect.Module1DetectDTO;
import com.financialfinishieldguard.data.aiService.module1Detect.Module1DetectVO;
import com.financialfinishieldguard.data.aiService.module2Detect.Module2DetectDTO;
import com.financialfinishieldguard.data.aiService.module2Detect.Module2DetectVO;
import com.financialfinishieldguard.data.aiService.newDialogue.NewDialogueDTO;
import com.financialfinishieldguard.data.sessionService.HumanCustomerInfo;
import com.financialfinishieldguard.entity.User;
import com.financialfinishieldguard.gateutils.constants.UserContext;
import com.financialfinishieldguard.gateutils.exception.UserException;
import com.financialfinshieldguard.aiservice.mapper.AiServiceMapper;
import com.financialfinshieldguard.aiservice.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiServiceImpl extends ServiceImpl<AiServiceMapper, User> implements AiService {


    private final AiServiceMapper aiServiceMapper;

    @Value("${ai.url.ai-http-url}")
    private String httpUrl;

    @Autowired
    private MessageManager messageManager;

    public AiServiceImpl(AiServiceMapper aiServiceMapper) {
        this.aiServiceMapper = aiServiceMapper;
    }

    /**
     * 获取用户当前对话信息
     *
     * @return
     */
    @Override
    public GetCurrentUserDialoguesVO getCurrentUserDialogues() {
        try {
            //获取当前用户ID
            Long userId = UserContext.getCurrentId();

            //创建httpclient对象
            CloseableHttpClient httpClients = HttpClients.createDefault();

            // 使用 URIBuilder 构造带有查询参数的 URL
            URIBuilder uriBuilder = new URIBuilder(httpUrl + "/api/get_chathistory_name");
            //传当前用户的ID
            uriBuilder.addParameter("user_id", userId.toString());

            // 获取 URI
            URI uri = uriBuilder.build();

            //创建请求对象
            HttpGet httpGet = new HttpGet(uri);

            //发送请求对象,并接受响应结果
            CloseableHttpResponse response = httpClients.execute(httpGet);

            //获取服务端返回回来的状态码
            int statusCode = response.getStatusLine().getStatusCode();

            System.out.println("服务端返回的状态码: " + statusCode);

            //获取服务端返回回来的响应体，然后通过一个工具类来解析这个响应体
            HttpEntity entity = response.getEntity();
            String body = EntityUtils.toString(entity);

            System.out.println("服务端返回的数据是: " + body);

            // 将 JSON 数据转换为 UserDialogueInfo 的列表
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Long> map = objectMapper.readValue(body, new TypeReference<Map<String, Long>>() {
            });

            List<UserDialogueInfo> userDialogueInfoList = map.entrySet().stream()
                    .map(entry -> new UserDialogueInfo()
                            .setDialogueName(entry.getKey())
                            .setDialogueId(entry.getValue()))
                    .collect(Collectors.toList());
            ;

            // 输出转换后的列表
            log.info("转换后的userDialogueInfoList：{}", userDialogueInfoList);

            GetCurrentUserDialoguesVO result = new GetCurrentUserDialoguesVO();
            result.setUserDialogues(userDialogueInfoList);

            response.close();
            httpClients.close();

            return result;

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取当前选择的对话的记录
     *
     * @param sessionId
     * @return
     */
    @Override
    public String getDialogue(Long sessionId) {
        try {
            //创建httpclient对象
            CloseableHttpClient httpClients = HttpClients.createDefault();

            // 使用 URIBuilder 构造带有查询参数的 URL
            URIBuilder uriBuilder = new URIBuilder(httpUrl + "/api/get_chathistory");
            //传当前用户的ID
            uriBuilder.addParameter("session_id", "1");

            // 获取 URI
            URI uri = uriBuilder.build();

            //创建请求对象
            HttpGet httpGet = new HttpGet(uri);

            //发送请求对象,并接受响应结果
            CloseableHttpResponse response = httpClients.execute(httpGet);

            //获取服务端返回回来的状态码
            int statusCode = response.getStatusLine().getStatusCode();

            System.out.println("服务端返回的状态码: " + statusCode);

            //获取服务端返回回来的响应体，然后通过一个工具类来解析这个响应体
            HttpEntity entity = response.getEntity();
            String body = EntityUtils.toString(entity);

            System.out.println("服务端返回的数据是: " + body);

////            // 通过WebSocket发送数据
//            chatEndpoint.sendMessageToUser(body);


            //直接返回给前端就行

            response.close();
            httpClients.close();

            return body;

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 传图片文件，分析诈骗情况
     *
     * @param file
     * @return
     */
    @Override
    public AnalyseImageVO analyseImage(MultipartFile file) {
        try {
            // 创建 HttpClient 对象
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // 使用 URIBuilder 构造带有查询参数的 URL
            URIBuilder uriBuilder = new URIBuilder(httpUrl + "/api/module2/process-image");
            //传当前用户的ID
            uriBuilder.addParameter("print_yn", "false");

            // 获取 URI
            URI uri = uriBuilder.build();

            //创建请求对象
            HttpPost httpPost = new HttpPost(uri);

            // 检查传入的文件是否为空
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("文件不能为空");
            }

            // 使用 MultipartEntityBuilder 构建请求体
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody(
                    "image", // 参数名
                    file.getInputStream(), // 图片文件对象
                    ContentType.create("image/png"), // 图片文件的 MIME 类型
                    file.getOriginalFilename() // 图片文件名
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

            ObjectMapper objectMapper = new ObjectMapper();
            //注意：这里可以直接转是因为body中的参数名与实体类中的名字一模一样！！！一一对应！！！
            AnalyseImageVO analyseImageVO = objectMapper.readValue(body, AnalyseImageVO.class);

            //关闭资源
            response.close();
            httpClient.close();

            return analyseImageVO;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 传图片文件，分析图片文字
     *
     * @param file
     * @return
     */
    @Override
    public AnalyseImageTextVO analyseImageText(MultipartFile file) {
        try {
            // 创建 HttpClient 对象
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // 使用 URIBuilder 构造带有查询参数的 URL
            URIBuilder uriBuilder = new URIBuilder(httpUrl + "/paddle_ocr");

            // 获取 URI
            URI uri = uriBuilder.build();

            //创建请求对象
            HttpPost httpPost = new HttpPost(uri);

            // 检查传入的文件是否为空
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("文件不能为空");
            }

            // 使用 MultipartEntityBuilder 构建请求体
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody(
                    "file", // 参数名
                    file.getInputStream(), // 图片文件对象
                    ContentType.create("image/png"), // 图片文件的 MIME 类型
                    file.getOriginalFilename() // 图片文件名
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

            ObjectMapper objectMapper = new ObjectMapper();
            //注意：这里可以直接转是因为body中的参数名与实体类中的名字一模一样！！！一一对应！！！
            AnalyseImageTextVO analyseImageTextVO = objectMapper.readValue(body, AnalyseImageTextVO.class);

            //关闭资源
            response.close();
            httpClient.close();

            return analyseImageTextVO;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成一个新对话
     *
     * @param dialogueDTO
     */
    @Override
    public String newDialogue(NewDialogueDTO dialogueDTO) {
        try {
            // 创建 HttpClient 对象
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // 使用 URIBuilder 构造带有查询参数的 URL
            URIBuilder uriBuilder = new URIBuilder(httpUrl + "/api/new_chathistory");
            uriBuilder.addParameter("user_id", dialogueDTO.getUserId().toString());
            uriBuilder.addParameter("character_type", dialogueDTO.getCharacterType());
            uriBuilder.addParameter("name", dialogueDTO.getName());
            uriBuilder.addParameter("description", dialogueDTO.getDescription());

            // 获取 URI
            URI uri = uriBuilder.build();

            //创建请求对象
            HttpPost httpPost = new HttpPost(uri);

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

            //关闭资源
            response.close();
            httpClient.close();

            return body;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 交易数据欺诈系数判定
     *
     * @param request
     * @return
     */
    @Override
    public Module1DetectVO detect1(Module1DetectDTO request) {
        try {
            // 创建 HttpClient 对象
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // 创建请求对象
            HttpPost httpPost = new HttpPost(httpUrl + "/api/module1/detect");

            // 使用 ObjectMapper 将 Module1DetectDTO 转换为 JSON 字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(request);

            // 设置请求体
            StringEntity requestEntity = new StringEntity(
                    json,
                    "UTF-8"
            );
            requestEntity.setContentType("application/json");
            httpPost.setEntity(requestEntity);

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

            //注意：这里可以直接转是因为body中的参数名与实体类中的名字一模一样！！！一一对应！！！

            Module1DetectVO module1DetectVO = objectMapper.readValue(body, Module1DetectVO.class);

            //关闭资源
            response.close();
            httpClient.close();

            return module1DetectVO;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对文本或图片url进行风险判定
     * @param request
     * @return
     */
    @Override
    public Module2DetectVO detect2(Module2DetectDTO request) {
        //过滤
        //服务端返回的数据是: {"detail":"错误: 400: content_type 仅支持 text/image"}
        if (!request.getContent_type().equals("text") && !request.getContent_type().equals("image")) {
            throw new UserException("content_type 仅支持 text/image");
        }

        try {
            // 创建 HttpClient 对象
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // 创建请求对象
            HttpPost httpPost = new HttpPost(httpUrl + "/api/module2/detect");

            // 使用 ObjectMapper 将 Module1DetectDTO 转换为 JSON 字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(request);

            // 设置请求体
            StringEntity requestEntity = new StringEntity(
                    json,
                    "UTF-8"
            );
            requestEntity.setContentType("application/json");
            httpPost.setEntity(requestEntity);

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

            //注意：这里可以直接转是因为body中的参数名与实体类中的名字一模一样！！！一一对应！！！
            Module2DetectVO module2DetectVO = objectMapper.readValue(body, Module2DetectVO.class);

            //关闭资源
            response.close();
            httpClient.close();

            return module2DetectVO;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<HumanCustomerInfo> getHumanCustomerUserIds() {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("role", 3);
        List<User> list = this.list(userQueryWrapper);
        List<HumanCustomerInfo> collect = list.stream().map(user -> {
            HumanCustomerInfo humanCustomerInfo = new HumanCustomerInfo();
            BeanUtils.copyProperties(user, humanCustomerInfo);
            humanCustomerInfo.setUserId(user.getUserId().toString());
            return humanCustomerInfo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public AnalyseAudioVO analyseAudio(MultipartFile file) {
        try {
            // 创建 HttpClient 对象
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // 创建请求对象
            HttpPost httpPost = new HttpPost(httpUrl + "/anti_spoof_score");

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

//            //现在改成ws，本来就要传JSON结构，就不需要再转实体类了！直接把body发给前端
//            messageManager.sendMessageToUserByUserId(UserContext.getCurrentId(), body);
            ObjectMapper objectMapper = new ObjectMapper();
            AnalyseAudioVO analyseAudioVO = objectMapper.readValue(body, AnalyseAudioVO.class);

            //关闭资源
            response.close();
            httpClient.close();

            return analyseAudioVO;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
