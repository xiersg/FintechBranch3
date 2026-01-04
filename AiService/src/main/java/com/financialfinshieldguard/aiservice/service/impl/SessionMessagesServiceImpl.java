package com.financialfinshieldguard.aiservice.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.financialfinishieldguard.data.sessionService.getSessionHistory.GetSessionHistoryVO;
import com.financialfinishieldguard.data.sessionService.getSessionHistory.SessionHistory;
import com.financialfinishieldguard.data.sessionService.saveMessage.SaveMessageDTO;
import com.financialfinishieldguard.entity.SessionMessages;
import com.financialfinishieldguard.entity.User;
import com.financialfinishieldguard.gateutils.constants.UserContext;
import com.financialfinshieldguard.aiservice.service.SessionMessagesService;
import com.financialfinshieldguard.aiservice.mapper.SessionMessagesMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 20316
 * @description 针对表【session_messages(会话消息表)】的数据库操作Service实现
 * @createDate 2025-10-18 14:49:09
 */
@Service
public class SessionMessagesServiceImpl extends ServiceImpl<SessionMessagesMapper, SessionMessages> implements SessionMessagesService {

    /**
     * 保存消息
     *
     * @param request
     */
    @Override
    public void saveMessage(Integer fromType, SaveMessageDTO request) {
        SessionMessages sessionMessages = new SessionMessages();

        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        Long id = snowflake.nextId();
        Integer messageType = fromType;//0-AI  1-用户
        BeanUtils.copyProperties(request, sessionMessages);

        sessionMessages.setId(id).setMessageType(messageType);

        this.save(sessionMessages);
    }

    /**
     * 根据对话ID获取历史记录
     *
     * @return
     */
    @Override
    public GetSessionHistoryVO getHistoryBySessionId(Long sessionId) {


        QueryWrapper<SessionMessages> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId);
        List<SessionMessages> list = this.list(queryWrapper);

        List<SessionHistory> collect = list.stream().map(sessionMessages -> {
            SessionHistory sessionHistory = new SessionHistory();
            BeanUtils.copyProperties(sessionMessages, sessionHistory);
            return sessionHistory;
        }).collect(Collectors.toList());

        GetSessionHistoryVO vo = new GetSessionHistoryVO();
        vo.setList(collect);
        return vo;
    }


    /**
     * 根据对话ID获取历史记录(给AI的，只要最近20条)
     *
     * @return
     */
    @Override
    public List<SessionMessages> getHistoryBySessionIdToAI(Long sessionId) {
        QueryWrapper<SessionMessages> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId)
                .orderByAsc("created_at") // 按 created_at 字段降序排列
                .last("LIMIT 20"); // 限制查询结果为最近的 20 条记录
        return this.list(queryWrapper);
    }
}




