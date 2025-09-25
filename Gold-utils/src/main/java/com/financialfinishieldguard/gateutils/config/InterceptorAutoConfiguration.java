package com.financialfinishieldguard.gateutils.config;

import com.financialfinishieldguard.gateutils.interceptors.UserInfoInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterceptorAutoConfiguration {

    @Bean
    public UserInfoInterceptor userInfoInterceptor() {
        return new UserInfoInterceptor();
    }
}
