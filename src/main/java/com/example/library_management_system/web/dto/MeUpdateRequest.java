package com.example.library_management_system.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MeUpdateRequest {

    // 仅允许读者修改用户名；管理员不允许通过该接口修改用户名
    @Size(max = 64)
    private String username;

    @NotBlank
    @Size(max = 64)
    private String name;

    @Size(max = 32)
    private String phone;

    @Email
    @Size(max = 128)
    private String email;

    @Size(max = 512)
    private String avatarUrl;
}

