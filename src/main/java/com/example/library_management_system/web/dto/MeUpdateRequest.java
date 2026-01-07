package com.example.library_management_system.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MeUpdateRequest {

    @NotBlank
    @Size(max = 64)
    private String name;

    @Size(max = 32)
    private String phone;

    @Email
    @Size(max = 128)
    private String email;
}

