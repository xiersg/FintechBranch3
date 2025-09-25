package com.financialfinshieldguard.complaintservice.mapper;

import com.financialfinishieldguard.entity.Complaints;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 20316
* @description 针对表【complaints(投诉信息表)】的数据库操作Mapper
* @createDate 2025-09-25 17:24:39
* @Entity com.financialfinishieldguard.entity.Complaints
*/
@Mapper
public interface ComplaintsMapper extends BaseMapper<Complaints> {

}




