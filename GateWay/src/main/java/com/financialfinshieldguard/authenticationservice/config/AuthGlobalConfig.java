package com.financialfinshieldguard.authenticationservice.config;

import com.financialfinshieldguard.authenticationservice.filter.AuthGlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthGlobalConfig {

    @Bean
    public AuthGlobalFilter authGlobalFilter(AuthProperties authProperties) {
        return new AuthGlobalFilter(authProperties);
    }
}
