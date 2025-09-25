package com.financialfinshieldguard.authenticationservice.filter;

import com.financialfinshieldguard.authenticationservice.config.AuthProperties;
import com.financialfinshieldguard.authenticationservice.constants.AuthConstant;
import com.financialfinshieldguard.authenticationservice.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取request
        ServerHttpRequest request = exchange.getRequest();

        //判断是否要做登录拦截
        if (isExclude(request.getPath().toString())) {
            return chain.filter(exchange);
        }
        //获取token
        List<String> headers = request.getHeaders().get(AuthConstant.TOKEN);
        String token = null;
        if (headers != null && !headers.isEmpty()) {
            token = headers.get(0);
        }
        Claims claims = null;

        try {
            //校验并解析token
            claims = JwtUtil.parse(token);
        } catch (Exception e) {
            //拦截，设置响应状态码为401
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }


        //传递用户信息
        System.out.println("Claims: " + claims); // 打印 Claims 内容

        String userIdStr = (String)claims.get(AuthConstant.USER_ID);
        String role = (String)claims.get(AuthConstant.ROLE);

        ServerWebExchange swe = exchange.mutate()
                .request(builder -> builder
                        .header(AuthConstant.USER_ID, userIdStr)
                        .header(AuthConstant.ROLE, role))
                .build();

        //放行
        return chain.filter(swe);
    }


    private boolean isExclude(String path) {
        for (String excludePath : authProperties.getExcludePaths()) {
            if (antPathMatcher.match(excludePath, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
