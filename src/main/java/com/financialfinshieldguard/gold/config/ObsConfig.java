package com.financialfinshieldguard.gold.config;

import com.obs.services.ObsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObsConfig {

    @Value("${hwy.obs.endPoint}")
    private String endpoint;

    @Value("${hwy.obs.access-key}")
    private String accessKey;

    @Value("${hwy.obs.secret-key}")
    private String secretKey;

    @Bean
    public ObsClient obsClient() {
        return new ObsClient(accessKey, secretKey, endpoint);
    }
}
