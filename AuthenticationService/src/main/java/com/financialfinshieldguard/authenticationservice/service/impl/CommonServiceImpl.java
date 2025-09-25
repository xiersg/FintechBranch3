package com.financialfinshieldguard.authenticationservice.service.impl;


import com.financialfinishieldguard.data.common.sms.SMSDTO;
import com.financialfinishieldguard.data.common.sms.SMSVO;
import com.financialfinishieldguard.data.common.uploadUrl.UploadUrlDTO;
import com.financialfinishieldguard.data.common.uploadUrl.UploadUrlVO;
import com.financialfinishieldguard.gateutils.constants.ExceptionConstant;
import com.financialfinishieldguard.gateutils.constants.user.redisConstant;
import com.financialfinishieldguard.gateutils.utils.RandomNumUtil;
import com.financialfinishieldguard.gateutils.utils.SendMailUtil;
import com.financialfinshieldguard.authenticationservice.HweiOBSUtil;
import com.financialfinshieldguard.authenticationservice.OBSConstant;
import com.financialfinishieldguard.gateutils.exception.CodeException;
import com.financialfinshieldguard.authenticationservice.service.CommonService;
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
    public SMSVO sendSms(SMSDTO request) {
        String phone = request.getPhone();
        String code = RandomNumUtil.getRandomNum();

        redisTemplate.opsForValue().set(redisConstant.REGISTER_CODE + phone, code, 5, TimeUnit.SECONDS);
        SendMailUtil.sendEmailCode(phone, code);

        return new SMSVO().setPhone(phone);
    }

    /**
     * 发送QQ邮箱
     * @param targetMail
     * @return
     */
    @Override
    public SMSVO sendMail(String targetMail, String type) {
        String code = RandomNumUtil.getRandomNum();

        String prefix = "";
        if (redisConstant.LORGIN.equals(type)) {
            prefix = redisConstant.LOGIN_CODE;
        } else if (redisConstant.REGISTER.equals(type)) {
            prefix = redisConstant.REGISTER_CODE;
        } else if (redisConstant.CHANGEPASSWORD.equals(type)) {
            prefix = redisConstant.CHANGEPASSWORD_CODE;
        } else {
            throw new CodeException(ExceptionConstant.NOT_CORRECT_TYPE);
        }
        redisTemplate.opsForValue().set(prefix + targetMail, code, 60, TimeUnit.SECONDS);
        SendMailUtil.sendEmailCode(targetMail, code);

        return new SMSVO().setPhone(targetMail);

    }

    @Override
    public UploadUrlVO getUploadUrl(UploadUrlDTO request) {
        String fileName = request.getFileName();

        String uploadUrl = hweiOBSUtil.uploadUrl(fileName, OBSConstant.PICTURE_EXPIRE_TIME);
        String downUrl = hweiOBSUtil.downUrl(fileName, OBSConstant.PICTURE_EXPIRE_TIME);
        UploadUrlVO response = new UploadUrlVO();
        response.setUploadUrl(uploadUrl)
                .setDownloadUrl(downUrl);

        return response;
    }
}
