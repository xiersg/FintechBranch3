package com.financialfinshieldguard.authenticationservice.controller;

import com.financialfinishieldguard.data.common.Result;
import com.financialfinishieldguard.data.common.sms.SMSDTO;
import com.financialfinishieldguard.data.common.sms.SMSVO;
import com.financialfinishieldguard.data.common.uploadUrl.UploadUrlDTO;
import com.financialfinishieldguard.data.common.uploadUrl.UploadUrlVO;
import com.financialfinshieldguard.authenticationservice.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@Slf4j
/**
 * @Controller + @ResponseBody：
 * @Controller：标记类为 Spring MVC 控制器。
 * @ResponseBody：标记方法返回值为 HTTP 响应体。
 */
@RestController
/**
 * @RequestMapping：
 * 用于定义请求映射，可以指定请求路径、方法类型等。
 */
@RequestMapping("/user/common")
public class CommonController {

    @Autowired
    private CommonService commonService;


    //发送阿里云邮箱
    @GetMapping("/sms")
    public Result<SMSVO> sedSms(@Valid SMSDTO request) {
        SMSVO response = commonService.sendSms(request);

        return Result.OK(response);
    }

    //发送qq邮箱
    // controller
    @GetMapping("/getCode")
    public Result<String> mail(@RequestParam("targetEmail") String targetEmail, @RequestParam("type") String type) {
        commonService.sendMail(targetEmail, type);
        return Result.OK("邮箱发送成功");
    }

    @GetMapping("/uploadUrl")
    public Result<UploadUrlVO> getUploadUrl(@Valid UploadUrlDTO request){
        UploadUrlVO response = commonService.getUploadUrl(request);

        return Result.OK(response);
    }


}
