package com.example.library_management_system.config;

import com.example.library_management_system.domain.entity.User;
import com.example.library_management_system.domain.enums.UserRole;
import com.example.library_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            userRepository.findByUsername("admin").orElseGet(() -> {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .name("管理员")
                        .role(UserRole.ADMIN)
                        .enabled(true)
                        .build();
                return userRepository.save(admin);
            });
        };
    }
}

