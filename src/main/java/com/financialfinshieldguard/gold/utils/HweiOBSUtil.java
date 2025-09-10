package com.financialfinshieldguard.gold.utils;

import com.obs.services.ObsClient;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.HttpMethodEnum;
import com.obs.services.model.TemporarySignatureResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class HweiOBSUtil {

    @Resource
    private ObsClient obsClient;

    @Value("${hwy.obs.bucketName}")
    private String bucketName;

    @Value("${hwy.obs.path}")
    private String obsPath;

    /**
     * 生成上传文件的预签名URL（PUT方法）
     */
    @SneakyThrows
    public String uploadUrl(String fileName, Integer expires, String fileType) {
        TemporarySignatureRequest request = new TemporarySignatureRequest(
                HttpMethodEnum.PUT,
                expires
        );

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/octet-stream");

        String objectName = fileName.concat(".").concat(fileType);

        request.setBucketName(bucketName);
        request.setObjectKey(objectName);
        request.setHeaders(headers);

        return obsClient.createTemporarySignature(request).getSignedUrl();
    }

    /**
     * 生成下载文件的URL（GET方法）
     */
    public String downUrl(String fileName, Integer expires, String fileType) {
        TemporarySignatureRequest request = new TemporarySignatureRequest(
                HttpMethodEnum.GET,
                expires
        );
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/octet-stream");

        String objectName = fileName.concat(".").concat(fileType);

        request.setBucketName(bucketName);
        request.setObjectKey(objectName);
        request.setHeaders(headers);

        return obsClient.createTemporarySignature(request).getSignedUrl();
    }


    public void deleteFile(String objectName) {
        obsClient.deleteObject(bucketName, objectName);
    }
}