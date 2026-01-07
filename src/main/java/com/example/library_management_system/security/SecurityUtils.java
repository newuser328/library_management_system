package com.example.library_management_system.security;

import com.example.library_management_system.domain.entity.User;
import com.example.library_management_system.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public User currentUser(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Unauthenticated");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }
}

