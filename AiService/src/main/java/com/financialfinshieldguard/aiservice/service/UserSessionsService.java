package com.financialfinshieldguard.aiservice.service;

import com.financialfinishieldguard.data.sessionService.createUserSession.CreateUserSessionDTO;
import com.financialfinishieldguard.data.sessionService.createUserSession.CreateUserSessionVO;
import com.financialfinishieldguard.data.sessionService.getUserSession.GetUserSessionVO;
import com.financialfinishieldguard.entity.UserSessions;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 20316
* @description 针对表【user_sessions(用户会话表)】的数据库操作Service
* @createDate 2025-10-18 14:49:09
*/
public interface UserSessionsService extends IService<UserSessions> {

    /**
     * 创建对话
     * @param request
     * @return
     */
    CreateUserSessionVO createUserSession(CreateUserSessionDTO request);

    /**
     * 获取当前用户所有对话
     * @return
     */
    GetUserSessionVO getUserSessions();
}
