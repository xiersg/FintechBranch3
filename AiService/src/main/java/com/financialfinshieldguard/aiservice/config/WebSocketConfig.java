package com.financialfinshieldguard.aiservice.config;

import com.financialfinishieldguard.gateutils.utils.JwtUtil;
import com.financialfinshieldguard.aiservice.ws.ChatEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {

    @Autowired
    private ApplicationContext applicationContext;


    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        ChatEndpoint.setApplicationContext(applicationContext);
        return new ServerEndpointExporter();
    }

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }
}