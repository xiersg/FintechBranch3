package com.financialfinshieldguard.complaintservice.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.financialfinishieldguard.data.complaint.ComplaintDTO;
import com.financialfinishieldguard.data.complaint.ComplaintVO;
import com.financialfinishieldguard.data.complaint.GetComplaintsInfoVO;
import com.financialfinishieldguard.entity.Complaints;

/**
* @author 20316
* @description 针对表【complaints(投诉信息表)】的数据库操作Service
* @createDate 2025-09-25 17:24:39
*/
public interface ComplaintsService extends IService<Complaints> {

    /***
     * 用户投诉
     * @param request
     * @return
     */
    ComplaintVO complaint(ComplaintDTO request);

    /**
     * 获取投诉列表
     * @return
     */
    GetComplaintsInfoVO getComplaintsInfo();
}
