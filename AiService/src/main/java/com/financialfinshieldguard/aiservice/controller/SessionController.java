package com.financialfinshieldguard.aiservice.controller;


import com.financialfinishieldguard.data.common.Result;
import com.financialfinishieldguard.data.sessionService.createUserSession.CreateUserSessionDTO;
import com.financialfinishieldguard.data.sessionService.createUserSession.CreateUserSessionVO;
import com.financialfinishieldguard.data.sessionService.getSessionHistory.GetSessionHistoryDTO;
import com.financialfinishieldguard.data.sessionService.getSessionHistory.GetSessionHistoryVO;
import com.financialfinishieldguard.data.sessionService.getSessionHistory.SessionHistory;
import com.financialfinishieldguard.data.sessionService.getUserSession.GetUserSessionVO;
import com.financialfinishieldguard.entity.SessionMessages;
import com.financialfinshieldguard.aiservice.service.SessionMessagesService;
import com.financialfinshieldguard.aiservice.service.UserSessionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/session")
public class SessionController {

    @Autowired
    private UserSessionsService userSessionsService;

    @Autowired
    private SessionMessagesService sessionMessagesService;
    /**
     * 创建对话
     * @param request
     * @return
     */
    @PostMapping("/create")
    public Result<CreateUserSessionVO> createUserSession(@RequestBody CreateUserSessionDTO request) {
        CreateUserSessionVO response = userSessionsService.createUserSession(request);
        return Result.OK(response);
    }

    /**
     * 获取当前用户所有对话
     * @return
     */
    @GetMapping()
    public Result<GetUserSessionVO> getUserSessions() {
        GetUserSessionVO response = userSessionsService.getUserSessions();
        return Result.OK(response);
    }


    @GetMapping("/history")
    public Result<GetSessionHistoryVO> getSessionHistory(GetSessionHistoryDTO request) {
        GetSessionHistoryVO response = sessionMessagesService.getHistoryBySessionId(request.getSessionId());

        return Result.OK(response);
    }
}
