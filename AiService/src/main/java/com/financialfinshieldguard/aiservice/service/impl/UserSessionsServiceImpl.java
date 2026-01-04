package com.financialfinshieldguard.aiservice.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.financialfinishieldguard.data.sessionService.createUserSession.CreateUserSessionDTO;
import com.financialfinishieldguard.data.sessionService.createUserSession.CreateUserSessionVO;
import com.financialfinishieldguard.data.sessionService.getUserSession.GetUserSessionVO;
import com.financialfinishieldguard.data.sessionService.getUserSession.UserSessionsVO;
import com.financialfinishieldguard.entity.UserSessions;
import com.financialfinishieldguard.gateutils.constants.UserContext;
import com.financialfinishieldguard.gateutils.exception.UserException;
import com.financialfinshieldguard.aiservice.service.UserSessionsService;
import com.financialfinshieldguard.aiservice.mapper.UserSessionsMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author 20316
* @description 针对表【user_sessions(用户会话表)】的数据库操作Service实现
* @createDate 2025-10-18 14:49:09
*/
@Service
public class UserSessionsServiceImpl extends ServiceImpl<UserSessionsMapper, UserSessions> implements UserSessionsService{

    /**
     * 创建对话
     * @param request
     * @return
     */
    @Override
    public CreateUserSessionVO createUserSession(CreateUserSessionDTO request) {
        Long userId = UserContext.getCurrentId();
        String sessionName = request.getSessionName();

        //检查当前这个对话名称是否在当前用户的对话列表中已存在
        QueryWrapper<UserSessions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("session_name", sessionName);

        UserSessions u = this.getOne(queryWrapper);
        if (u != null) {
            throw new UserException("当前对话名在当前用户的对话列表中已存在");
        }

        //雪花算法（Snowflake）
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        Long sessionId = snowflake.nextId();

        UserSessions userSessions = new UserSessions();
        userSessions.setSessionId(sessionId);
        userSessions.setUserId(userId);
        userSessions.setSessionName(sessionName);

        this.save(userSessions);

        return new CreateUserSessionVO().setSessionId(sessionId.toString());
    }

    /**
     * 获取当前用户所有对话
     * @return
     */
    @Override
    public GetUserSessionVO getUserSessions() {
        Long userId = UserContext.getCurrentId();
        QueryWrapper<UserSessions> queryWrapper = new QueryWrapper<>();
        //降序获取感觉普遍一些
        queryWrapper.eq("user_id", userId).orderByDesc("created_at");

        List<UserSessions> list = this.list(queryWrapper);
        List<UserSessionsVO> collect = list.stream().map(l -> {
            UserSessionsVO v = new UserSessionsVO();
            BeanUtils.copyProperties(l, v);
            v.setSessionId(l.getSessionId().toString());
            v.setUserId(l.getUserId().toString());
            return v;
        }).collect(Collectors.toList());
        return new GetUserSessionVO().setUserSessions(collect);
    }
}




