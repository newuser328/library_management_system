package com.example.library_management_system.web.dto;

import com.example.library_management_system.domain.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserCreateUpdateRequest {

    @NotBlank
    private String username;

    // 创建用户时必填；更新用户时如不想修改密码，可不传该字段或传空字符串
    private String password;

    @NotBlank
    private String name;

    private String phone;

    private String email;

    @NotNull
    private UserRole role;

    private Boolean enabled;
}

