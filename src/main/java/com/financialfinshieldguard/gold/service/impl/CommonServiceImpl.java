package com.financialfinshieldguard.gold.service.impl;


import com.financialfinshieldguard.gold.constants.config.OBSConstant;
import com.financialfinshieldguard.gold.constants.user.redisConstant;
import com.financialfinshieldguard.gold.data.common.sms.SMSRequest;
import com.financialfinshieldguard.gold.data.common.sms.SMSResponse;
import com.financialfinshieldguard.gold.data.common.uploadUrl.UploadUrlRequest;
import com.financialfinshieldguard.gold.data.common.uploadUrl.UploadUrlResponse;
import com.financialfinshieldguard.gold.exception.CodeException;
import com.financialfinshieldguard.gold.service.CommonService;
import com.financialfinshieldguard.gold.utils.HweiOBSUtil;
import com.financialfinshieldguard.gold.utils.RandomNumUtil;
import com.financialfinshieldguard.gold.utils.SendMailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private HweiOBSUtil hweiOBSUtil;

    @Override
    public SMSResponse sendSms(SMSRequest request) {
        String phone = request.getPhone();
        String code = RandomNumUtil.getRandomNum();

        redisTemplate.opsForValue().set(redisConstant.REGISTER_CODE + phone, code, 5, TimeUnit.SECONDS);
        new SendMailUtil().sendEmailCode(phone, code);

        return new SMSResponse().setPhone(phone);
    }

    /**
     * 发送QQ邮箱
     * @param targetMail
     * @return
     */
    @Override
    public SMSResponse sendMail(String targetMail, String type) {
        String code = new RandomNumUtil().getRandomNum();

        String prefix = "";
        if (redisConstant.LORGIN.equals(type)) {
            prefix = redisConstant.LOGIN_CODE;
        } else if (redisConstant.REGISTER.equals(type)) {
            prefix = redisConstant.REGISTER_CODE;
        } else if (redisConstant.CHANGEPASSWORD.equals(type)) {
            prefix = redisConstant.CHANGEPASSWORD_CODE;
        } else {
            throw new CodeException("传入的type类型未知");
        }
        redisTemplate.opsForValue().set(prefix + targetMail, code, 60, TimeUnit.SECONDS);
        new SendMailUtil().sendEmailCode(targetMail, code);

        return new SMSResponse().setPhone(targetMail);

    }

    @Override
    public UploadUrlResponse getUploadUrl(UploadUrlRequest request) {
        String fileName = request.getFileName();

        String uploadUrl = hweiOBSUtil.uploadUrl(fileName, OBSConstant.PICTURE_EXPIRE_TIME, request.getFileType());
        String downUrl = hweiOBSUtil.downUrl(fileName, OBSConstant.PICTURE_EXPIRE_TIME, request.getFileType());
        UploadUrlResponse response = new UploadUrlResponse();
        response.setUploadUrl(uploadUrl)
                .setDownloadUrl(downUrl);

        return response;
    }
}
