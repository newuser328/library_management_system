package com.example.library_management_system.service;

import com.example.library_management_system.domain.entity.User;
import com.example.library_management_system.domain.enums.UserRole;
import com.example.library_management_system.repository.UserRepository;
import com.example.library_management_system.security.JwtTokenService;
import com.example.library_management_system.web.dto.AdminRegisterRequest;
import com.example.library_management_system.web.dto.RegisterRequest;
import com.example.library_management_system.web.dto.SmsCodeRequest;
import com.example.library_management_system.web.dto.SmsLoginRequest;
import com.example.library_management_system.web.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AdminRegisterTokenService adminRegisterTokenService;
    private final SmsCodeService smsCodeService;
    private final SmsService smsService;

    /**
     * 注册默认读者账号。注册成功后直接返回JWT token，方便前端“注册即登录”。
     */
    @Transactional
    public String register(@Valid RegisterRequest req) {
        assertUsernameNotExists(req.getUsername());

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .role(UserRole.READER)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        return jwtTokenService.generateToken(saved.getId(), saved.getUsername(), saved.getRole().name());
    }

    /**
     * 管理员注册：使用已登录管理员生成的一次性口令（24小时有效）。
     */
    @Transactional
    public String registerAdmin(@Valid AdminRegisterRequest req) {
        var tokenEntity = adminRegisterTokenService.validateAvailable(req.getAdminToken());

        assertUsernameNotExists(req.getUsername());

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        adminRegisterTokenService.markUsed(tokenEntity);
        return jwtTokenService.generateToken(saved.getId(), saved.getUsername(), saved.getRole().name());
    }

    /**
     * 发送短信验证码
     */
    public void sendSmsCode(@Valid SmsCodeRequest req) {
        String phone = req.getPhone();

        // 检查发送频率限制
        if (!smsCodeService.canSend(phone)) {
            throw new IllegalStateException("发送过于频繁，请稍后再试");
        }

        // 生成验证码
        String code = smsCodeService.generateCode();

        // 保存验证码到Redis
        smsCodeService.saveCode(phone, code);

        // 发送短信
        try {
            boolean success = smsService.sendCode(phone, code);
            if (!success) {
                throw new IllegalStateException("短信发送失败，请稍后重试");
            }
        } catch (RuntimeException e) {
            // 捕获并重新抛出，保留详细错误信息
            throw new IllegalStateException("短信发送失败: " + e.getMessage(), e);
        }

        // 记录发送时间（用于频率限制）
        smsCodeService.recordSendTime(phone);
    }

    /**
     * 验证码登录（如果手机号未注册，自动创建读者账号）
     * 返回LoginResponse，包含token和needSetPassword标识
     */
    @Transactional
    public LoginResponse smsLogin(@Valid SmsLoginRequest req) {
        String phone = req.getPhone();
        String code = req.getCode();

        // 验证验证码
        if (!smsCodeService.verifyCode(phone, code)) {
            throw new IllegalStateException("验证码错误或已过期");
        }

        // 查找用户（根据手机号）
        User user = userRepository.findByPhone(phone).orElse(null);

        // 如果用户不存在，自动创建读者账号
        boolean needSetPassword = false;
        if (user == null) {
            // 生成默认用户名（手机号）
            String username = phone;
            // 如果用户名已存在，添加后缀
            int suffix = 1;
            while (userRepository.findByUsername(username).isPresent()) {
                username = phone + "_" + suffix;
                suffix++;
            }

            user = User.builder()
                    .username(username)
                    .password("") // 空密码：用于标识“必须设置密码”
                    .name("用户" + phone.substring(7)) // 默认名称使用手机号后4位
                    .phone(phone)
                    .role(UserRole.READER)
                    .enabled(true)
                    .build();

            user = userRepository.save(user);
            needSetPassword = true;
        } else {
            // 检查用户是否需要设置密码（手机号验证码注册用户 password 为空）
            String password = user.getPassword();
            if (password == null || password.isBlank()) {
                needSetPassword = true;
            }
        }

        // 生成JWT token（包含needSetPassword标识）
        String token = jwtTokenService.generateToken(user.getId(), user.getUsername(), user.getRole().name(), needSetPassword);
        return new LoginResponse(token, needSetPassword);
    }

    private void assertUsernameNotExists(String username) {
        userRepository.findByUsername(username).ifPresent(u -> {
            throw new IllegalStateException("username already exists");
        });
    }
}
