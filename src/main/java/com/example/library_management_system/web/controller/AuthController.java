package com.example.library_management_system.web.controller;

import com.example.library_management_system.domain.entity.User;
import com.example.library_management_system.repository.UserRepository;
import com.example.library_management_system.security.JwtTokenService;
import com.example.library_management_system.web.dto.ApiResponse;
import com.example.library_management_system.web.dto.LoginRequest;
import com.example.library_management_system.service.AuthService;
import com.example.library_management_system.web.dto.LoginResponse;
import com.example.library_management_system.web.dto.AdminRegisterRequest;
import com.example.library_management_system.web.dto.RegisterRequest;
import com.example.library_management_system.web.dto.SmsCodeRequest;
import com.example.library_management_system.web.dto.SmsLoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 检查是否需要设置密码（密码为空字符串加密后的结果）
        boolean needSetPassword = false;
        String password = user.getPassword();
        if (password != null && passwordEncoder.matches("", password)) {
            needSetPassword = true;
        }

        String token = jwtTokenService.generateToken(user.getId(), user.getUsername(), user.getRole().name(), needSetPassword);
        return ApiResponse.ok(new LoginResponse(token, needSetPassword));
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        String token = authService.register(request);
        return ApiResponse.ok(new LoginResponse(token, false));
    }

    /**
     * 管理员注册：需要提供 adminSecret（服务端配置的口令）。
     */
    @PostMapping("/register-admin")
    public ApiResponse<LoginResponse> registerAdmin(@Valid @RequestBody AdminRegisterRequest request) {
        String token = authService.registerAdmin(request);
        return ApiResponse.ok(new LoginResponse(token, false));
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    public ApiResponse<Void> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        authService.sendSmsCode(request);
        return ApiResponse.ok(null);
    }

    /**
     * 验证码登录（如果手机号未注册，自动创建读者账号）
     */
    @PostMapping("/sms/login")
    public ApiResponse<LoginResponse> smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        LoginResponse response = authService.smsLogin(request);
        return ApiResponse.ok(response);
    }
}

