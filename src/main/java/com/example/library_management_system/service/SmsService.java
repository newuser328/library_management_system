package com.example.library_management_system.service;

/**
 * 短信服务接口
 */
public interface SmsService {
    /**
     * 发送验证码短信
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 是否发送成功
     */
    boolean sendCode(String phone, String code);
}
