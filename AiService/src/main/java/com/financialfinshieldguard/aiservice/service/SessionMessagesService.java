package com.financialfinshieldguard.aiservice.service;

import com.financialfinishieldguard.data.sessionService.getSessionHistory.GetSessionHistoryVO;
import com.financialfinishieldguard.data.sessionService.saveMessage.SaveMessageDTO;
import com.financialfinishieldguard.entity.SessionMessages;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 20316
* @description 针对表【session_messages(会话消息表)】的数据库操作Service
* @createDate 2025-10-18 14:49:09
*/
public interface SessionMessagesService extends IService<SessionMessages> {

    /**
     * 保存消息
     * @param fromType 0 AI  1 用户
     * @param request
     */
    void saveMessage(Integer fromType,SaveMessageDTO request);

    /**
     * 根据对话ID获取历史记录
     * @return
     */
    GetSessionHistoryVO getHistoryBySessionId(Long sessionId);

    /**
     * 根据对话ID获取历史记录(给AI的，只要最近20条)
     * @param sessionId
     * @return
     */
    List<SessionMessages> getHistoryBySessionIdToAI(Long sessionId);
}
