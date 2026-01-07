package com.example.library_management_system.web.controller;

import com.example.library_management_system.security.SecurityUtils;
import com.example.library_management_system.service.AdminRegisterTokenService;
import com.example.library_management_system.web.dto.AdminRegisterTokenResponse;
import com.example.library_management_system.web.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin-register-tokens")
@RequiredArgsConstructor
public class AdminRegisterTokenController {

    private final AdminRegisterTokenService tokenService;
    private final SecurityUtils securityUtils;

    /**
     * 仅管理员可生成管理员注册口令，有效期 24 小时。
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminRegisterTokenResponse> generate(Authentication authentication) {
        var admin = securityUtils.currentUser(authentication);
        var token = tokenService.generate(admin);
        return ApiResponse.ok(new AdminRegisterTokenResponse(token.getToken(), token.getExpiresAt()));
    }
}

