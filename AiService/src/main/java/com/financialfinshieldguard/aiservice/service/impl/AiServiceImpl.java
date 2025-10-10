package com.financialfinshieldguard.aiservice.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialfinishieldguard.data.aiService.analyseAudio.AnalyseAudioVO;
import com.financialfinishieldguard.data.aiService.analyseImage.AnalyseImageVO;
import com.financialfinishieldguard.data.aiService.getCurrentUserDialogues.GetCurrentUserDialoguesVO;
import com.financialfinishieldguard.data.aiService.getCurrentUserDialogues.UserDialogueInfo;
import com.financialfinishieldguard.gateutils.constants.UserContext;
import com.financialfinshieldguard.aiservice.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    /**
     * 获取用户当前对话信息
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
            URIBuilder uriBuilder = new URIBuilder("http://13425.free.idcfengye.com/api/get_chathistory_name");
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

            System.out.println("服务端返回的状态码: " +statusCode);

            //获取服务端返回回来的响应体，然后通过一个工具类来解析这个响应体
            HttpEntity entity = response.getEntity();
            String body = EntityUtils.toString(entity);

            System.out.println("服务端返回的数据是: " +body);

            // 将 JSON 数据转换为 UserDialogueInfo 的列表
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Long> map = objectMapper.readValue(body, new TypeReference<Map<String, Long>>() {});

            List<UserDialogueInfo> userDialogueInfoList = map.entrySet().stream()
                    .map(entry -> new UserDialogueInfo()
                            .setDialogueName(entry.getKey())
                            .setDialogueId(entry.getValue()))
                    .collect(Collectors.toList());;

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
     * @param sessionId
     * @return
     */
    @Override
    public String getDialogue(Long sessionId) {
        try {
            //创建httpclient对象
            CloseableHttpClient httpClients = HttpClients.createDefault();

            // 使用 URIBuilder 构造带有查询参数的 URL
            URIBuilder uriBuilder = new URIBuilder("http://13425.free.idcfengye.com/api/get_chathistory");
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

            System.out.println("服务端返回的状态码: " +statusCode);

            //获取服务端返回回来的响应体，然后通过一个工具类来解析这个响应体
            HttpEntity entity = response.getEntity();
            String body = EntityUtils.toString(entity);

            System.out.println("服务端返回的数据是: " +body);

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
     * 传音频文件，判断AI率
     * @param file
     * @return
     */
    @Override
    public AnalyseAudioVO analyseAudio(MultipartFile file) {
        try {
            // 创建 HttpClient 对象
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // 创建请求对象
            HttpPost httpPost = new HttpPost("http://13425.free.idcfengye.com/anti_spoof_score");

//            // 创建音频文件对象
//            File audioFile = new File("C:\\Users\\20316\\Desktop\\test_audio.wav");

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

//        // 添加其他参数
//        builder.addTextBody("username", "admin", ContentType.TEXT_PLAIN);
//        builder.addTextBody("password", "123456", ContentType.TEXT_PLAIN);

            // 构建 HttpEntity
            HttpEntity multipartEntity = builder.build();

            // 设置请求体
            httpPost.setEntity(multipartEntity);
            //发送请求
            CloseableHttpResponse response = httpClient.execute(httpPost);
            //解析返回结果
            //获取服务端返回回来的状态码
            int statusCode = response.getStatusLine().getStatusCode();

            System.out.println("服务端返回的状态码: " +statusCode);

            //获取服务端返回回来的响应体，然后通过一个工具类来解析这个响应体
            HttpEntity entity1 = response.getEntity();
            String body = EntityUtils.toString(entity1);

            System.out.println("服务端返回的数据是: " +body);

            ObjectMapper objectMapper = new ObjectMapper();
            // 将JSON字符串转换为AnalyseAudioVO实体类
            //注意：这里可以直接转是因为body中的参数名与实体类中的名字一模一样！！！一一对应！！！
            AnalyseAudioVO analyseAudioVO = objectMapper.readValue(body, AnalyseAudioVO.class);

            //关闭资源
            response.close();
            httpClient.close();

            return analyseAudioVO;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 传图片文件，分析诈骗情况
     * @param file
     * @return
     */
    @Override
    public AnalyseImageVO analyseImage(MultipartFile file) {
        try {
            // 创建 HttpClient 对象
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // 使用 URIBuilder 构造带有查询参数的 URL
            URIBuilder uriBuilder = new URIBuilder("http://13425.free.idcfengye.com/api/module2/process-image");
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

            System.out.println("服务端返回的状态码: " +statusCode);

            //获取服务端返回回来的响应体，然后通过一个工具类来解析这个响应体
            HttpEntity entity1 = response.getEntity();
            String body = EntityUtils.toString(entity1);

            System.out.println("服务端返回的数据是: " +body);

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
}
