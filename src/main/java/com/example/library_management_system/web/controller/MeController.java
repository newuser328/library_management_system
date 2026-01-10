package com.example.library_management_system.web.controller;

import com.example.library_management_system.domain.entity.User;
import com.example.library_management_system.service.MeService;
import com.example.library_management_system.web.dto.ApiResponse;
import com.example.library_management_system.web.dto.ChangePasswordRequest;
import com.example.library_management_system.web.dto.MeUpdateRequest;
import com.example.library_management_system.web.dto.SetPasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final MeService meService;

    @GetMapping
    public ApiResponse<User> me(Authentication authentication) {
        return ApiResponse.ok(meService.getByUsername(authentication.getName()));
    }

    @PutMapping("/profile")
    public ApiResponse<User> updateProfile(Authentication authentication,
                                          @Valid @RequestBody MeUpdateRequest req) {
        return ApiResponse.ok(meService.updateProfile(authentication.getName(), req));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(Authentication authentication,
                                           @Valid @RequestBody ChangePasswordRequest req) {
        meService.changePassword(authentication.getName(), req);
        return ApiResponse.ok(null);
    }

    /**
     * 首次设置密码（用于手机号验证码注册的用户）
     */
    @PostMapping("/set-password")
    public ApiResponse<Void> setPassword(Authentication authentication,
                                       @Valid @RequestBody SetPasswordRequest req) {
        meService.setPassword(authentication.getName(), req);
        return ApiResponse.ok(null);
    }
}
