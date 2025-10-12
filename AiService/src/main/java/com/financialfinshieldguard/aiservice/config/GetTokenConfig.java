package com.financialfinshieldguard.aiservice.config;

import com.financialfinishieldguard.gateutils.constants.AuthConstant;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;

public class GetTokenConfig extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        //从请求头中获取token
        List<String> token = request.getHeaders().get(AuthConstant.TOKEN);
        if (token != null && !token.isEmpty()) {
            String t = token.get(0);
            //将token保存到EndpointConfig的用户属性中
            sec.getUserProperties().put(AuthConstant.TOKEN, t);
        }
    }

}


