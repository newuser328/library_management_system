package com.example.library_management_system.service.impl;

import com.cloopen.rest.sdk.BodyType;
import com.cloopen.rest.sdk.CCPRestSmsSDK;
import com.example.library_management_system.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * 容联云通信短信服务实现
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.sms.provider", havingValue = "yuntongxun")
public class YuntongxunSmsService implements SmsService {

    @Value("${app.sms.yuntongxun.server-ip}")
    private String serverIp;

    @Value("${app.sms.yuntongxun.server-port}")
    private String serverPort;

    @Value("${app.sms.yuntongxun.account-sid}")
    private String accountSId;

    @Value("${app.sms.yuntongxun.account-token}")
    private String accountToken;

    @Value("${app.sms.yuntongxun.app-id}")
    private String appId;

    @Value("${app.sms.yuntongxun.template-id}")
    private String templateId;

    @Value("${app.sms.code-expire-seconds:300}")
    private int codeExpireSeconds;

    @Override
    public boolean sendCode(String phone, String code) {
        try {
            log.info("开始发送短信，手机号: {}, 模板ID: {}", phone, templateId);
            
            CCPRestSmsSDK sdk = new CCPRestSmsSDK();
            sdk.init(serverIp, serverPort);
            sdk.setAccount(accountSId, accountToken);
            sdk.setAppId(appId);
            sdk.setBodyType(BodyType.Type_JSON);

            // 模板参数：验证码和有效期（分钟）
            String[] datas = {code, String.valueOf(codeExpireSeconds / 60)};
            log.debug("模板参数: {}", java.util.Arrays.toString(datas));

            HashMap<String, Object> result = sdk.sendTemplateSMS(phone, templateId, datas);
            
            log.info("容联云通信返回结果: {}", result);

            String statusCode = (String) result.get("statusCode");
            if ("000000".equals(statusCode)) {
                log.info("短信发送成功，手机号: {}", phone);
                return true;
            } else {
                String errorMsg = (String) result.get("statusMsg");
                log.error("短信发送失败，手机号: {}, 错误码: {}, 错误信息: {}", 
                    phone, statusCode, errorMsg);
                // 将错误信息保存到异常中，方便上层获取
                throw new RuntimeException(String.format("短信发送失败: 错误码=%s, 错误信息=%s", statusCode, errorMsg));
            }
        } catch (RuntimeException e) {
            // 重新抛出运行时异常（包含错误信息）
            throw e;
        } catch (Exception e) {
            log.error("发送短信异常，手机号: {}, 异常信息: {}", phone, e.getMessage(), e);
            throw new RuntimeException("发送短信时发生异常: " + e.getMessage(), e);
        }
    }
}
