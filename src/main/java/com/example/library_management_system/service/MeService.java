package com.example.library_management_system.service;

import com.example.library_management_system.domain.entity.User;
import com.example.library_management_system.repository.UserRepository;
import com.example.library_management_system.web.dto.ChangePasswordRequest;
import com.example.library_management_system.web.dto.MeUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    @Transactional
    public User updateProfile(String username, MeUpdateRequest req) {
        User user = getByUsername(username);
        user.setName(req.getName());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest req) {
        User user = getByUsername(username);

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new IllegalStateException("oldPassword incorrect");
        }
        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new IllegalStateException("newPassword cannot be same as old password");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }
}

