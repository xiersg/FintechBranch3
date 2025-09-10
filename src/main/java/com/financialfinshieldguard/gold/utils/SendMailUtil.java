package com.financialfinshieldguard.gold.utils;

import com.financialfinshieldguard.gold.constants.config.ConfigEnum;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class SendMailUtil {


    public static void sendEmailCode(String targetEmail, String authCode) {
        try {
            // 设置TLS协议
            System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
            // 创建邮箱对象
            SimpleEmail mail = new SimpleEmail();
            // 设置发送邮件的服务器
            mail.setHostName("smtp.qq.com");
            // "你的邮箱号"+ "上文开启SMTP获得的授权码"
            mail.setAuthentication(ConfigEnum.SMS_ACCESS_KEY_ID.getValue(), ConfigEnum.SMS_ACCESS_KEY_SECRET.getValue());
            // 发送邮件 "你的邮箱号"+"发送时用的昵称"
            mail.setFrom(ConfigEnum.SMS_ACCESS_KEY_ID.getValue(), "无双大帝");
            // 使用安全链接
            mail.setSSLOnConnect(true);
            // 接收用户的邮箱
            mail.addTo(targetEmail);
            // 邮件的主题(标题)
            mail.setSubject("注册验证码");
            // 邮件的内容
            mail.setMsg("您的验证码为:" + authCode+"(一分钟内有效)");
            // 发送
            mail.send();
        } catch (EmailException e) {
            e.printStackTrace();
        }
    }
}