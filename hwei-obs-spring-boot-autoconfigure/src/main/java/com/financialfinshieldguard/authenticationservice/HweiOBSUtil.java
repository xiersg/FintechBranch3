package com.financialfinshieldguard.authenticationservice;

import com.obs.services.ObsClient;
import com.obs.services.model.HttpMethodEnum;
import com.obs.services.model.TemporarySignatureRequest;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;


public class HweiOBSUtil {

    private ObsClient obsClient;

    private ObsConfig obsConfig;

    public ObsConfig getObsConfig() {
        return obsConfig;
    }

    public ObsClient getObsClient() {
        return obsClient;
    }

    public void setObsClient(ObsClient obsClient) {
        this.obsClient = obsClient;
    }

    public void setObsConfig(ObsConfig obsConfig) {
        this.obsConfig = obsConfig;
    }

    /**
     * 生成上传文件的预签名URL（PUT方法）
     */
    @SneakyThrows
    public String uploadUrl(String fileName, Integer expires) {
        TemporarySignatureRequest request = new TemporarySignatureRequest(
                HttpMethodEnum.PUT,
                expires
        );

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/octet-stream");

        request.setBucketName(obsConfig.getBucketName());
        request.setObjectKey(fileName);
        request.setHeaders(headers);

        return obsClient.createTemporarySignature(request).getSignedUrl();
    }

    /**
     * 生成下载文件的URL（GET方法）
     */
    public String downUrl(String fileName, Integer expires) {
        TemporarySignatureRequest request = new TemporarySignatureRequest(
                HttpMethodEnum.GET,
                expires
        );
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/octet-stream");

        request.setBucketName(obsConfig.getBucketName());
        request.setObjectKey(fileName);
        request.setHeaders(headers);

        return obsClient.createTemporarySignature(request).getSignedUrl();
    }


    public void deleteFile(String objectName) {
        obsClient.deleteObject(obsConfig.getBucketName(), objectName);
    }
}