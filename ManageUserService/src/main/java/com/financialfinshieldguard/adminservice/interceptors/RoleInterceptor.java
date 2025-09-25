package com.financialfinshieldguard.adminservice.interceptors;

import cn.hutool.core.util.StrUtil;
import com.financialfinishieldguard.gateutils.constants.AuthConstant;
import com.financialfinishieldguard.gateutils.constants.enumm.RoleEnum;
import com.financialfinishieldguard.gateutils.constants.user.ErrorEnum;
import com.financialfinishieldguard.gateutils.exception.UserException;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取登录用户信息
        String role = request.getHeader(AuthConstant.ROLE);


        //判断是否获取了用户职责，如果有，判断是否为管理员
        if (!StrUtil.isNotBlank(role)) {
            throw new UserException(ErrorEnum.ROLE_ERROR);
        }
        if (!role.equals(RoleEnum.ADMIN.getValue().toString())) {
            //判断是否是管理员，如果不是则抛出异常
            throw new UserException(ErrorEnum.ROLE_NOT_PERMISSION);
        }

        //放行
        return true;
    }
}
