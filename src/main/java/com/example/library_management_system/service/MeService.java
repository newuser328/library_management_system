package com.example.library_management_system.service;

import com.example.library_management_system.domain.entity.User;
import com.example.library_management_system.repository.UserRepository;
import com.example.library_management_system.web.dto.ChangePasswordRequest;
import com.example.library_management_system.web.dto.MeUpdateRequest;
import com.example.library_management_system.web.dto.SetPasswordRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    @Transactional
    public User updateProfile(String username, MeUpdateRequest req) {
        User user = getByUsername(username);

        // 仅允许读者修改用户名；管理员不允许通过该接口修改用户名
        if (req.getUsername() != null) {
            String newUsername = req.getUsername().trim();

            if (!newUsername.isEmpty()) {
                if (user.getRole() != com.example.library_management_system.domain.enums.UserRole.READER) {
                    throw new IllegalStateException("管理员不允许修改用户名");
                }

                // 基础格式校验：仅允许字母/数字/下划线，且 3-64 位
                if (!newUsername.matches("^[a-zA-Z0-9_]{3,64}$")) {
                    throw new IllegalStateException("用户名格式不正确（仅支持字母/数字/下划线，长度3-64）");
                }

                // 唯一性校验
                userRepository.findByUsername(newUsername).ifPresent(u -> {
                    if (!u.getId().equals(user.getId())) {
                        throw new IllegalStateException("用户名已存在");
                    }
                });

                user.setUsername(newUsername);
            }
        }

        user.setName(req.getName());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());

        if (req.getAvatarUrl() != null) {
            user.setAvatarUrl(req.getAvatarUrl());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest req) {
        User user = getByUsername(username);

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new IllegalStateException("oldPassword incorrect");
        }
        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new IllegalStateException("newPassword cannot be same as old password");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * 首次设置密码（用于手机号验证码注册的用户）
     */
    @Transactional
    public void setPassword(String username, SetPasswordRequest req) {
        User user = getByUsername(username);
        
        // 仅允许“手机号验证码自动注册”的用户设置密码：这类用户 password 为空/空白
        String currentPassword = user.getPassword();
        if (currentPassword != null && !currentPassword.isBlank()) {
            throw new IllegalStateException("密码已设置，请使用修改密码功能");
        }

        // 设置新密码
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);
    }
}

