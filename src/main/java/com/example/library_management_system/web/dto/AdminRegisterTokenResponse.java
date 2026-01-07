package com.example.library_management_system.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminRegisterTokenResponse {
    private String token;
    private LocalDateTime expiresAt;
}

