package com.financialfinshieldguard.aiservice;

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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.net.URI;

@SpringBootTest
class AiServiceApplicationTests {

    /**
     * 获取当前用户的对话记录
     * @throws Exception
     */
    @Test
    public void testGET() throws Exception {
        //创建httpclient对象
        CloseableHttpClient httpClients = HttpClients.createDefault();

        // 使用 URIBuilder 构造带有查询参数的 URL
        URIBuilder uriBuilder = new URIBuilder("http://13425.free.idcfengye.com/api/get_chathistory_name");
        //传当前用户的ID
        uriBuilder.addParameter("user_id", "1");

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
    }


    /**
     * 获取当前选择的对话的记录
     * @throws Exception
     */
    @Test
    public void testGETHistory() throws Exception {
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
    }

    /**
     * 传音频文件，判断AI率
     * @throws Exception
     */
    @Test
    public void testAudioPOST() throws Exception {
        // 创建 HttpClient 对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // 创建请求对象
        HttpPost httpPost = new HttpPost("http://13425.free.idcfengye.com/anti_spoof_score");

        // 创建音频文件对象
        File audioFile = new File("C:\\Users\\20316\\Desktop\\test_audio.wav");

        if (!audioFile.exists() || !audioFile.isFile()) {
            throw new IllegalArgumentException("文件不存在或不是一个有效的文件: " + audioFile.getAbsolutePath());
        }


        // 使用 MultipartEntityBuilder 构建请求体
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody(
                "file", // 参数名
                audioFile, // 音频文件对象
                ContentType.create("audio/wav"), // 音频文件的 MIME 类型
                audioFile.getName() // 音频文件名
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

        //关闭资源
        response.close();
        httpClient.close();
    }

    /**
     * 传图片文件，分析诈骗情况
     * @throws Exception
     */
    @Test
    public void testImagePOST() throws Exception {
        // 创建 HttpClient 对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 使用 URIBuilder 构造带有查询参数的 URL
        URIBuilder uriBuilder = new URIBuilder("http://13425.free.idcfengye.com/anti_spoof_score");
        //传当前用户的ID
        uriBuilder.addParameter("print_yn", "false");

        // 获取 URI
        URI uri = uriBuilder.build();

        //创建请求对象
        HttpPost httpPost = new HttpPost(uri);

        // 创建文件对象
        File file = new File("C:\\Users\\20316\\Pictures\\Screenshots\\屏幕截图 2024-09-10 152933.png");


        // 使用 MultipartEntityBuilder 构建请求体
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody(
                "image", // 参数名
                file, // 图片文件对象
                ContentType.create("image/png"), // 图片文件的 MIME 类型
                file.getName() // 图片文件名
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

        //关闭资源
        response.close();
        httpClient.close();
    }


    /**
     * 创建新对话
     * @throws Exception
     */
    @Test
    public void testNewDialoguePOST() throws Exception {
        // 创建 HttpClient 对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 使用 URIBuilder 构造带有查询参数的 URL
        URIBuilder uriBuilder = new URIBuilder("http://13425.free.idcfengye.com/api/new_chathistory");
        uriBuilder.addParameter("user_id", "1");
        uriBuilder.addParameter("character_type", "2");
        uriBuilder.addParameter("name", "3");
        uriBuilder.addParameter("description", "4");

        // 获取 URI
        URI uri = uriBuilder.build();

        //创建请求对象
        HttpPost httpPost = new HttpPost(uri);

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

        //关闭资源
        response.close();
        httpClient.close();
    }




}
