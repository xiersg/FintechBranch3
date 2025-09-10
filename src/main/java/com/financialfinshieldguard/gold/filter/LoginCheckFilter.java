package com.financialfinshieldguard.gold.filter;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialfinshieldguard.gold.constants.user.ErrorEnum;
import com.financialfinshieldguard.gold.data.common.Result;
import com.financialfinshieldguard.gold.exception.GlobalExceptionHandler;
import com.financialfinshieldguard.gold.exception.UserException;
import com.financialfinshieldguard.gold.model.User;
import com.financialfinshieldguard.gold.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@WebFilter(urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        //强转
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //获取请求URL
        String uri = request.getRequestURI().toString();

        //如果是登陆注册接口，直接放行返回
        if (uri.equals("/user/register") || uri.equals("/user/login") || uri.equals("/user/loginCode") || uri.equals("/user/changePassword") || uri.equals("/user/common/getCode")) {
            //放行
            chain.doFilter(request, response);
            return;
        }

        //如果是登录之后的接口，进行校验令牌操作

        //获取请求头token
        String jwt = request.getHeader("token");

        //使用工具类对jwt进行基本校验
        //hasLength:    return str != null && !str.isEmpty();
        //空字符串是指长度为 0 的字符串，即 `""`。
        //- `str.isEmpty()` 是一个字符串方法，用于检查字符串是否为空。
        //  - 如果字符串为空（长度为 0），返回 `true`。
        //  - 如果字符串不为空（长度大于 0），返回 `false`。
        if (!StringUtils.hasLength(jwt)) {
            sendErrorResponse(response, ErrorEnum.NO_USER_LOGIN);
            // 中断过滤器链
            return;
        }


        //校验token信息
        try {
            JwtUtil.parse(jwt);
        } catch (Exception e) {
            sendErrorResponse(response, ErrorEnum.JWT_ERROR);
            // 中断过滤器链
            return;
        }

        //放行
        chain.doFilter(request, response);
    }

    /**
     * 发送规范的错误信息给前端！！！(因为过滤器中抛出的全局异常处理器无法接收到，所以这里单独处理)
     *
     * 有重复逻辑，提取一下吧
     * @param response
     * @param errorEnum
     * @throws IOException
     */
    private void sendErrorResponse(HttpServletResponse response, ErrorEnum errorEnum) throws IOException {
        Result<Object> result = Result.UserError(errorEnum.getCode(), errorEnum.getMessage());

        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResult = objectMapper.writeValueAsString(result);

        // 使用 PrintWriter 写入响应内容
        PrintWriter writer = response.getWriter();
        writer.print(jsonResult);
        writer.flush();
        writer.close();

    }
}
