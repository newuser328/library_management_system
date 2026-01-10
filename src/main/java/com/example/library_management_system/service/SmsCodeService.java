package com.example.library_management_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务：生成、存储、验证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeService {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.sms.code-length:6}")
    private int codeLength;

    @Value("${app.sms.code-expire-seconds:300}")
    private long codeExpireSeconds;

    @Value("${app.sms.send-limit-seconds:60}")
    private long sendLimitSeconds;

    private static final String CODE_KEY_PREFIX = "sms:code:";
    private static final String LIMIT_KEY_PREFIX = "sms:limit:";

    /**
     * 生成验证码
     */
    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 保存验证码到Redis
     */
    public void saveCode(String phone, String code) {
        String key = CODE_KEY_PREFIX + phone;
        redisTemplate.opsForValue().set(key, code, codeExpireSeconds, TimeUnit.SECONDS);
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String phone, String code) {
        String key = CODE_KEY_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (code.equals(storedCode)) {
            // 验证成功后删除验证码（一次性使用）
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    /**
     * 检查是否可以发送（频率限制）
     */
    public boolean canSend(String phone) {
        String limitKey = LIMIT_KEY_PREFIX + phone;
        String value = redisTemplate.opsForValue().get(limitKey);
        return value == null;
    }

    /**
     * 记录发送时间（用于频率限制）
     */
    public void recordSendTime(String phone) {
        String limitKey = LIMIT_KEY_PREFIX + phone;
        redisTemplate.opsForValue().set(limitKey, "1", sendLimitSeconds, TimeUnit.SECONDS);
    }
}
