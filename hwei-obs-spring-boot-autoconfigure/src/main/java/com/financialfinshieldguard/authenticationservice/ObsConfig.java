package com.financialfinshieldguard.authenticationservice;

import com.obs.services.ObsClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Component：用于标记普通的组件类，使其成为 Spring 管理的 Bean。
//@Configuration：用于标记配置类，通常包含多个 @Bean 方法，用于定义和配置其他 Bean。(不过自己也会是Bean)
@ConfigurationProperties("hwy.obs")
@Data
public class ObsConfig {

    private String accessKey;

    private String secretKey;

    private String bucketName;

    private String endPoint;


}
