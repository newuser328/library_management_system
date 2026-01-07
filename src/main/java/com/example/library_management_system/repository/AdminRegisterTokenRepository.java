package com.example.library_management_system.repository;

import com.example.library_management_system.domain.entity.AdminRegisterToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRegisterTokenRepository extends JpaRepository<AdminRegisterToken, Long> {
    Optional<AdminRegisterToken> findByToken(String token);
}

