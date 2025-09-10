//package com.financialfinshieldguard.gold.utils;
//
//import cn.hutool.core.util.StrUtil;
//import io.minio.GetPresignedObjectUrlArgs;
//import io.minio.MinioClient;
//import io.minio.http.Method;
//import lombok.SneakyThrows;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.util.concurrent.TimeUnit;
//
///**
// * 这段代码定义了一个名为 OSSUtil 的服务类，用于与 MinIO 对象存储服务进行交互。它提供了两个主要功能：
// * 生成上传文件的预签名 URL（uploadUrl 方法）。
// * 生成下载文件的 URL（downUrl 方法）。
// *
// * @Service：这是一个 Spring 注解，表示该类是一个服务组件。Spring 会自动检测并管理这些类的实例，使得它们可以被其他组件依赖注入。
// */
//@Service
//public class OSSUtil {
//
//    //@Resource：这是一个 Java 注解，用于依赖注入。
//    // 这里它将 MinioClient 的实例注入到 OSSUtil 类中，MinioClient 是 MinIO SDK 提供的客户端类，用于与 MinIO 服务进行交互。
//    @Resource
//    private MinioClient minioClient;
//
//    //@Value：这是一个 Spring 注解，用于将配置文件中的值注入到字段中。这里它将配置文件中 minio.url 的值注入到 url 字段中，minio.url 通常是一个 MinIO 服务的访问地址。
//    @Value("${minio.url}")
//    private String url;
//
//    /**
//     * 生成上传文件的预签名 URL
//     *
//     * @param bucketName 存储桶名称
//     * @param objectName 象名称（文件名）
//     * @param expires 预签名 URL 的过期时间
//     * @return
//     *
//     * @SneakyThrows：这是一个 Lombok 注解，用于在不声明抛出异常的情况下，处理可能抛出的受检查异常。这使得代码更加简洁，但需要注意的是，它可能会隐藏潜在的异常问题。
//     */
//    @SneakyThrows
//    public String uploadUrl(String bucketName, String objectName, Integer expires) {
//
//        //minioClient.getPresignedObjectUrl：这是 MinIO SDK 提供的方法，用于生成一个预签名的 URL。预签名 URL 允许在没有直接访问 MinIO 服务的情况下，上传或下载文件。
//        return minioClient.getPresignedObjectUrl(
//                //GetPresignedObjectUrlArgs.builder()：这是一个构建器模式的使用，用于构建 GetPresignedObjectUrlArgs 对象，该对象定义了生成预签名 URL 所需的参数。
//                GetPresignedObjectUrlArgs.builder()
//                        //method(Method.PUT)：指定 HTTP 方法为 PUT，这通常用于上传文件。
//                        .method(Method.PUT)
//                        //bucket(bucketName)：指定存储桶名称。
//                        //object(objectName)：指定对象名称（文件名）。
//                        //expiry(expires, TimeUnit.SECONDS)：指定预签名 URL 的过期时间，单位为秒。
//                        .bucket(bucketName)
//                        .object(objectName)
//                        .expiry(expires, TimeUnit.SECONDS)
//                        .build());
//    }
//
//    /**
//     * 生成下载文件的 URL
//     *
//     * @param bucketName
//     * @param fileName
//     * @return
//     */
//    public String downUrl(String bucketName, String fileName) {
//        // oss地址/bucketName/fileName
//
//        //StrUtil.SLASH：这是 Hutool 工具类库中的一个常量，表示斜杠（/）。
//        // Hutool 是一个 Java 工具类库，提供了许多便捷的方法，StrUtil 是其中的字符串工具类。
//
//        // 这里假设 MinIO 服务的 URL 格式为 http://minio-url/bucketName/fileName。
//        return url + StrUtil.SLASH + bucketName + StrUtil.SLASH + fileName;
//    }
//}
