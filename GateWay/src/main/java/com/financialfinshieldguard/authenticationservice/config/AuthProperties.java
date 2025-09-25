package com.financialfinshieldguard.authenticationservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties("wuxuan.auth")
public class AuthProperties {

    private List<String> excludePaths;
}
