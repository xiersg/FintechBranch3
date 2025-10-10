package com.financialfinshieldguard.aiservice.config;

import com.financialfinishieldguard.gateutils.interceptors.UserInfoInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
//所有微服务都有SpringMvc而网关没有!!!网关那里依赖引用了当前的Common服务,而当前Common服务是基于SpringMvc,但是网关底层不是SpringMvc,所以会出问题
//因此要让Common(我这里放在utils服务)在网关那里不生效,因此加上这个条件过滤网关
@ConditionalOnClass(DispatcherServlet.class)
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //不设置拦截路径,则默认就是拦截所有路径
        registry.addInterceptor(new UserInfoInterceptor());
    }
}