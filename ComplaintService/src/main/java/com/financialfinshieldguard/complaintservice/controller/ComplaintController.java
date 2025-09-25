package com.financialfinshieldguard.complaintservice.controller;


import com.financialfinishieldguard.data.common.Result;
import com.financialfinishieldguard.data.complaint.ComplaintDTO;
import com.financialfinishieldguard.data.complaint.ComplaintVO;
import com.financialfinishieldguard.data.complaint.GetComplaintsInfoVO;
import com.financialfinishieldguard.gateutils.constants.UserContext;
import com.financialfinshieldguard.complaintservice.service.ComplaintsService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/complaint")
public class ComplaintController {

    @Autowired
    private ComplaintsService complaintsService;

    @PostMapping
    public Result<ComplaintVO> complaint(@RequestBody @Valid ComplaintDTO request) {
        ComplaintVO response = complaintsService.complaint(request);

        log.info("userId:{}", UserContext.getCurrentId());
        return Result.OK(response);
    }

    @GetMapping
    public Result<GetComplaintsInfoVO> getComplaints() {
        GetComplaintsInfoVO response = complaintsService.getComplaintsInfo();

        return Result.OK(response);
    }
}
