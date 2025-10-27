package com.financialfinshieldguard.aiservice.config;

import com.financialfinishieldguard.gateutils.constants.AuthConstant;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.net.URI;
import java.util.List;

public class GetTokenConfig extends ServerEndpointConfig.Configurator {

//    @Override
//    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
//        //从请求头中获取token
//        List<String> token = request.getHeaders().get(AuthConstant.TOKEN);
//        if (token != null && !token.isEmpty()) {
//            String t = token.get(0);
//            //将token保存到EndpointConfig的用户属性中
//            sec.getUserProperties().put(AuthConstant.TOKEN, t);
//        }
//    }

    /**
     * 从url中获取token
     * @param sec
     * @param request
     * @param response
     */
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        // 从 URL 的查询参数中获取 Token
        String token = extractTokenFromUrl(request.getRequestURI());

        if (token != null) {
            // 将 Token 保存到 EndpointConfig 的用户属性中
            sec.getUserProperties().put(AuthConstant.TOKEN, token);
        }
    }


    // 辅助方法：从 URL 中提取 Token
    private String extractTokenFromUrl(URI requestUri) {
        if (requestUri == null || requestUri.getQuery() == null) {
            return null;
        }

        String query = requestUri.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                if ("token".equals(key)) {
                    return value;
                }
            }
        }
        return null;
    }
}


