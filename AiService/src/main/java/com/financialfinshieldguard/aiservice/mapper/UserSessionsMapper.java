package com.financialfinshieldguard.aiservice.mapper;

import com.financialfinishieldguard.entity.UserSessions;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 20316
* @description 针对表【user_sessions(用户会话表)】的数据库操作Mapper
* @createDate 2025-10-18 14:49:09
* @Entity com.financialfinishieldguard.entity.UserSessions
*/
@Mapper
public interface UserSessionsMapper extends BaseMapper<UserSessions> {

}




