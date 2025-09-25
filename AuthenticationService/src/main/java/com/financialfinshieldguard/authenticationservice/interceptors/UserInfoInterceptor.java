package com.financialfinshieldguard.authenticationservice.interceptors;

import cn.hutool.core.util.StrUtil;
import com.financialfinishieldguard.gateutils.constants.AuthConstant;
import com.financialfinishieldguard.gateutils.constants.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取登录用户信息
        String userId = request.getHeader(AuthConstant.USER_ID);

        //判断是否获取了用户，如果有，存入ThreadLocal
        if (StrUtil.isNotBlank(userId)) {
            UserContext.setCurrentId(Long.valueOf(userId));
        }

        //放行
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清理用户
        UserContext.removeCurrentId();
    }
}
