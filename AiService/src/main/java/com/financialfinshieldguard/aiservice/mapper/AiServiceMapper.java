package com.financialfinshieldguard.aiservice.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.financialfinishieldguard.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiServiceMapper extends BaseMapper<User> {

}
