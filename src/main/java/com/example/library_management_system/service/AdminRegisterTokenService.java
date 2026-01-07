package com.example.library_management_system.service;

import com.example.library_management_system.domain.entity.AdminRegisterToken;
import com.example.library_management_system.domain.entity.User;
import com.example.library_management_system.repository.AdminRegisterTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminRegisterTokenService {

    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int TOKEN_LEN = 24;
    private static final int EXPIRE_HOURS = 24;

    private final AdminRegisterTokenRepository tokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AdminRegisterToken generate(User createdBy) {
        String token = randomToken();
        // 简化：若极小概率碰撞，重试一次
        if (tokenRepository.findByToken(token).isPresent()) {
            token = randomToken();
        }

        LocalDateTime now = LocalDateTime.now();
        AdminRegisterToken entity = AdminRegisterToken.builder()
                .token(token)
                .createdAt(now)
                .expiresAt(now.plusHours(EXPIRE_HOURS))
                .used(false)
                .createdBy(createdBy)
                .build();
        return tokenRepository.save(entity);
    }

    @Transactional
    public AdminRegisterToken validateAvailable(String token) {
        AdminRegisterToken t = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("admin register token not found"));

        if (Boolean.TRUE.equals(t.getUsed())) {
            throw new IllegalStateException("admin register token already used");
        }
        if (t.getExpiresAt() != null && t.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("admin register token expired");
        }
        return t;
    }

    @Transactional
    public void markUsed(AdminRegisterToken token) {
        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);
    }

    private String randomToken() {
        StringBuilder sb = new StringBuilder(TOKEN_LEN);
        for (int i = 0; i < TOKEN_LEN; i++) {
            int idx = secureRandom.nextInt(CHARS.length());
            sb.append(CHARS.charAt(idx));
        }
        return sb.toString();
    }
}

