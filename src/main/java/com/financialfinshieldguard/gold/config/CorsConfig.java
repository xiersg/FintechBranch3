package com.financialfinshieldguard.gold.config;


//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
////允许所有跨域请求
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                // 为所有路径添加CORS映射
//                registry.addMapping("/**")
//                        // 允许所有域的请求
//                        .allowedOrigins("*")
//                        // 允许发送的请求方法，如GET、POST等
//                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                        // 允许的请求头
//                        .allowedHeaders("*")
//                        // 是否允许发送Cookie
//                        .allowCredentials(true);
//            }
//        };
//    }
//}


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // 为所有路径添加CORS映射
                registry.addMapping("/**")
                        // 允许的来源，这里设置为localhost:5175
                        .allowedOrigins("*")
//                        .allowedOrigins("http://192.168.1.100:5175")
                        // 允许发送的请求方法，如GET、POST等
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        // 允许的请求头
                        .allowedHeaders("*");
                        // 是否允许发送Cookie
                        //Credentials:凭据
//                        .allowCredentials(true);
            }
        };
    }
}