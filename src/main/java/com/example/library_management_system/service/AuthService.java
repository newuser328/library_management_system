package com.example.library_management_system.service;

import com.example.library_management_system.domain.entity.User;
import com.example.library_management_system.domain.enums.UserRole;
import com.example.library_management_system.repository.UserRepository;
import com.example.library_management_system.security.JwtTokenService;
import com.example.library_management_system.web.dto.AdminRegisterRequest;
import com.example.library_management_system.web.dto.RegisterRequest;
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

    private void assertUsernameNotExists(String username) {
        userRepository.findByUsername(username).ifPresent(u -> {
            throw new IllegalStateException("username already exists");
        });
    }
}
