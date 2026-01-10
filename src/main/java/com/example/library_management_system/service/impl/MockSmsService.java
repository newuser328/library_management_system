package com.example.library_management_system.service.impl;

import com.example.library_management_system.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 模拟短信服务（开发环境使用，验证码输出到日志）
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.sms.provider", havingValue = "mock", matchIfMissing = true)
public class MockSmsService implements SmsService {

    @Override
    public boolean sendCode(String phone, String code) {
        log.info("========== 模拟发送短信验证码 ==========");
        log.info("手机号: {}", phone);
        log.info("验证码: {}", code);
        log.info("=====================================");
        return true;
    }
}
