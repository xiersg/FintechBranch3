package com.financialfinshieldguard.gold.service;


import com.financialfinshieldguard.gold.annotation.Log;
import com.financialfinshieldguard.gold.data.common.sms.SMSRequest;
import com.financialfinshieldguard.gold.data.common.sms.SMSResponse;
import com.financialfinshieldguard.gold.data.common.uploadUrl.UploadUrlRequest;
import com.financialfinshieldguard.gold.data.common.uploadUrl.UploadUrlResponse;


public interface CommonService {
    SMSResponse sendSms(SMSRequest request);

    SMSResponse sendMail(String targetMail, String type);

    UploadUrlResponse getUploadUrl(UploadUrlRequest request);
}
