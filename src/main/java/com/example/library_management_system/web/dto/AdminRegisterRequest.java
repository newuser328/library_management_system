package com.example.library_management_system.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminRegisterRequest {

    @NotBlank
    @Size(min = 3, max = 64)
    private String username;

    @NotBlank
    @Size(min = 6, max = 64)
    private String password;

    @NotBlank
    @Size(max = 64)
    private String name;

    @Size(max = 32)
    private String phone;

    @Email
    @Size(max = 128)
    private String email;

    /**
     * 由已登录管理员生成的一次性口令（24小时有效）。
     */
    @NotBlank
    private String adminToken;
}
