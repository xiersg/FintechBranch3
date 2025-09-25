package com.financialfinshieldguard.authenticationservice.service;


import com.financialfinishieldguard.data.common.sms.SMSDTO;
import com.financialfinishieldguard.data.common.sms.SMSVO;
import com.financialfinishieldguard.data.common.uploadUrl.UploadUrlDTO;
import com.financialfinishieldguard.data.common.uploadUrl.UploadUrlVO;

public interface CommonService {
    SMSVO sendSms(SMSDTO request);

    SMSVO sendMail(String targetMail, String type);

    UploadUrlVO getUploadUrl(UploadUrlDTO request);
}
