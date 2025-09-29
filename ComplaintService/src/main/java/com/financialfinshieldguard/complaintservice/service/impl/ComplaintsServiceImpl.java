package com.financialfinshieldguard.complaintservice.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.financialfinishieldguard.data.complaint.ComplaintDTO;
import com.financialfinishieldguard.data.complaint.ComplaintInfo;
import com.financialfinishieldguard.data.complaint.ComplaintVO;
import com.financialfinishieldguard.data.complaint.GetComplaintsInfoVO;
import com.financialfinishieldguard.entity.Complaints;
import com.financialfinishieldguard.gateutils.constants.user.ErrorEnum;
import com.financialfinishieldguard.gateutils.exception.DatabaseException;
import com.financialfinshieldguard.complaintservice.service.ComplaintsService;
import com.financialfinshieldguard.complaintservice.mapper.ComplaintsMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 20316
 * @description 针对表【complaints(投诉信息表)】的数据库操作Service实现
 * @createDate 2025-09-25 17:24:39
 */
@Service
public class ComplaintsServiceImpl extends ServiceImpl<ComplaintsMapper, Complaints> implements ComplaintsService {

    @Override
    public ComplaintVO complaint(ComplaintDTO request) {
        Complaints complaints = new Complaints();
        BeanUtils.copyProperties(request, complaints);

        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        complaints.setId(snowflake.nextId());

        boolean isComplaintSave = this.save(complaints);
        if (!isComplaintSave) {
            throw new DatabaseException(ErrorEnum.DATABASE_ERROR);
        }
        return new ComplaintVO().setComplainted(isComplaintSave);
    }

    @Override
    public GetComplaintsInfoVO getComplaintsInfo() {
        List<Complaints> list = this.list();

        List<ComplaintInfo> collect = list.stream()
                .map(k -> {
                    ComplaintInfo complaintInfo = new ComplaintInfo();
                    BeanUtils.copyProperties(k, complaintInfo);
                    return complaintInfo;
                })
                .collect(Collectors.toList());

        return new GetComplaintsInfoVO().setComplaints(collect);
    }
}




